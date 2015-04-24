package org.oyach.jmh.server.mysql.jdbc;

import com.mysql.jdbc.*;
import org.oyach.jmh.server.mysql.MysqlConstants;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/24
 * @since 0.0.1
 */
public class Buffer {
    static final int MAX_BYTES_TO_DUMP = 512;

    static final int NO_LENGTH_LIMIT = -1;

    static final long NULL_LENGTH = -1;

    private int bufLength = 0;

    private byte[] byteBuffer;

    private int position = 0;

    protected boolean wasMultiPacket = false;

    public Buffer(byte[] buf) {
        this.byteBuffer = buf;
        setBufLength(buf.length);
    }

    Buffer(int size) {
        this.byteBuffer = new byte[size];
        setBufLength(this.byteBuffer.length);
        this.position = MysqlConstants.HEADER_LENGTH;
    }

    public final void clear() {
        this.position = MysqlConstants.HEADER_LENGTH;
    }

    public final void dump() {
        dump(getBufLength());
    }

    public final String dump(int numBytes) {
        return StringUtils.dumpAsHex(getBytes(0, numBytes > getBufLength() ? getBufLength() : numBytes), numBytes > getBufLength() ? getBufLength() : numBytes);
    }

    public final String dumpClampedBytes(int numBytes) {
        int numBytesToDump = numBytes < MAX_BYTES_TO_DUMP ? numBytes : MAX_BYTES_TO_DUMP;

        String dumped = StringUtils.dumpAsHex(getBytes(0, numBytesToDump > getBufLength() ? getBufLength() : numBytesToDump),
                numBytesToDump > getBufLength() ? getBufLength() : numBytesToDump);

        if (numBytesToDump < numBytes) {
            return dumped + " ....(packet exceeds max. dump length)";
        }

        return dumped;
    }

    public final void dumpHeader() {
        for (int i = 0; i < MysqlConstants.HEADER_LENGTH; i++) {
            String hexVal = Integer.toHexString(readByte(i) & 0xff);

            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }

            System.out.print(hexVal + " ");
        }
    }

    public final void dumpNBytes(int start, int nBytes) {
        StringBuilder asciiBuf = new StringBuilder();

        for (int i = start; (i < (start + nBytes)) && (i < getBufLength()); i++) {
            String hexVal = Integer.toHexString(readByte(i) & 0xff);

            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }

            System.out.print(hexVal + " ");

            if ((readByte(i) > 32) && (readByte(i) < 127)) {
                asciiBuf.append((char) readByte(i));
            } else {
                asciiBuf.append(".");
            }

            asciiBuf.append(" ");
        }

        System.out.println("    " + asciiBuf.toString());
    }

    public final void ensureCapacity(int additionalData) throws SQLException {
        if ((this.position + additionalData) > getBufLength()) {
            if ((this.position + additionalData) < this.byteBuffer.length) {
                // byteBuffer.length is != getBufLength() all of the time due to re-using of packets (we don't shrink them)
                //
                // If we can, don't re-alloc, just set buffer length to size of current buffer
                setBufLength(this.byteBuffer.length);
            } else {
                //
                // Otherwise, re-size, and pad so we can avoid allocing again in the near future
                //
                int newLength = (int) (this.byteBuffer.length * 1.25);

                if (newLength < (this.byteBuffer.length + additionalData)) {
                    newLength = this.byteBuffer.length + (int) (additionalData * 1.25);
                }

                if (newLength < this.byteBuffer.length) {
                    newLength = this.byteBuffer.length + additionalData;
                }

                byte[] newBytes = new byte[newLength];

                System.arraycopy(this.byteBuffer, 0, newBytes, 0, this.byteBuffer.length);
                this.byteBuffer = newBytes;
                setBufLength(this.byteBuffer.length);
            }
        }
    }

    /**
     * Skip over a length-encoded string
     *
     * @return The position past the end of the string
     */
    public int fastSkipLenString() {
        long len = this.readFieldLength();

        this.position += len;

        return (int) len;
    }

    public void fastSkipLenByteArray() {
        long len = this.readFieldLength();

        if (len == NULL_LENGTH || len == 0) {
            return;
        }

        this.position += len;
    }

    public final byte[] getBufferSource() {
        return this.byteBuffer;
    }

    public int getBufLength() {
        return this.bufLength;
    }

    /**
     * Returns the array of bytes this Buffer is using to read from.
     *
     * @return byte array being read from
     */
    public byte[] getByteBuffer() {
        return this.byteBuffer;
    }

    public final byte[] getBytes(int len) {
        byte[] b = new byte[len];
        System.arraycopy(this.byteBuffer, this.position, b, 0, len);
        this.position += len; // update cursor

        return b;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.mysql.jdbc.Buffer#getBytes(int, int)
     */
    public byte[] getBytes(int offset, int len) {
        byte[] dest = new byte[len];
        System.arraycopy(this.byteBuffer, offset, dest, 0, len);

        return dest;
    }

    public int getCapacity() {
        return this.byteBuffer.length;
    }

    public ByteBuffer getNioBuffer() {
        throw new IllegalArgumentException(Messages.getString("ByteArrayBuffer.0"));
    }

    /**
     * Returns the current position to write to/ read from
     *
     * @return the current position to write to/ read from
     */
    public int getPosition() {
        return this.position;
    }

    public final boolean isLastDataPacket() {
        return ((getBufLength() < 9) && ((this.byteBuffer[0] & 0xff) == 254));
    }

    public final boolean isAuthMethodSwitchRequestPacket() {
        return ((this.byteBuffer[0] & 0xff) == 254);
    }

    public final boolean isOKPacket() {
        return ((this.byteBuffer[0] & 0xff) == 0);
    }

    public final boolean isRawPacket() {
        return ((this.byteBuffer[0] & 0xff) == 1);
    }

    public final long newReadLength() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
            case 251:
                return 0;

            case 252:
                return readInt();

            case 253:
                return readLongInt();

            case 254: // changed for 64 bit lengths
                return readLongLong();

            default:
                return sw;
        }
    }

    public final byte readByte() {
        return this.byteBuffer[this.position++];
    }

    public final byte readByte(int readAt) {
        return this.byteBuffer[readAt];
    }

    public final long readFieldLength() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
            case 251:
                return NULL_LENGTH;

            case 252:
                return readInt();

            case 253:
                return readLongInt();

            case 254:
                return readLongLong();

            default:
                return sw;
        }
    }

    public final int readInt() {
        byte[] b = this.byteBuffer; // a little bit optimization

        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8);
    }

    public final int readIntAsLong() {
        byte[] b = this.byteBuffer;

        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8) | ((b[this.position++] & 0xff) << 16) | ((b[this.position++] & 0xff) << 24);
    }

    public final byte[] readLenByteArray(int offset) {
        long len = this.readFieldLength();

        if (len == NULL_LENGTH) {
            return null;
        }

        if (len == 0) {
            return Constants.EMPTY_BYTE_ARRAY;
        }

        this.position += offset;

        return getBytes((int) len);
    }

    public final long readLength() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
            case 251:
                return 0;

            case 252:
                return readInt();

            case 253:
                return readLongInt();

            case 254:
                return readLong();

            default:
                return sw;
        }
    }

    public final long readLong() {
        byte[] b = this.byteBuffer;

        return ((long) b[this.position++] & 0xff) | (((long) b[this.position++] & 0xff) << 8) | ((long) (b[this.position++] & 0xff) << 16)
                | ((long) (b[this.position++] & 0xff) << 24);
    }

    public final int readLongInt() {
        byte[] b = this.byteBuffer;

        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8) | ((b[this.position++] & 0xff) << 16);
    }

    public final long readLongLong() {
        byte[] b = this.byteBuffer;

        return (b[this.position++] & 0xff) | ((long) (b[this.position++] & 0xff) << 8) | ((long) (b[this.position++] & 0xff) << 16)
                | ((long) (b[this.position++] & 0xff) << 24) | ((long) (b[this.position++] & 0xff) << 32) | ((long) (b[this.position++] & 0xff) << 40)
                | ((long) (b[this.position++] & 0xff) << 48) | ((long) (b[this.position++] & 0xff) << 56);
    }

    public final int readnBytes() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
            case 1:
                return this.byteBuffer[this.position++] & 0xff;

            case 2:
                return this.readInt();

            case 3:
                return this.readLongInt();

            case 4:
                return (int) this.readLong();

            default:
                return 255;
        }
    }

    //
    // Read a null-terminated string
    //
    // To avoid alloc'ing a new byte array, we do this by hand, rather than calling getNullTerminatedBytes()
    //
    public final String readString() {
        int i = this.position;
        int len = 0;
        int maxLen = getBufLength();

        while ((i < maxLen) && (this.byteBuffer[i] != 0)) {
            len++;
            i++;
        }

        String s = StringUtils.toString(this.byteBuffer, this.position, len);
        this.position += (len + 1); // update cursor

        return s;
    }

    /**
     * Read string[NUL]
     *
     * @param encoding
     * @param exceptionInterceptor
     * @throws SQLException
     */
    public final String readString(String encoding, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        int i = this.position;
        int len = 0;
        int maxLen = getBufLength();

        while ((i < maxLen) && (this.byteBuffer[i] != 0)) {
            len++;
            i++;
        }

        try {
            return StringUtils.toString(this.byteBuffer, this.position, len, encoding);
        } catch (UnsupportedEncodingException uEE) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.1") + encoding + "'", SQLError.SQL_STATE_ILLEGAL_ARGUMENT,
                    exceptionInterceptor);
        } finally {
            this.position += (len + 1); // update cursor
        }
    }

    /**
     * Read string[$len]
     */
    public final String readString(String encoding, ExceptionInterceptor exceptionInterceptor, int expectedLength) throws SQLException {
        if (this.position + expectedLength > getBufLength()) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.2"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
        }

        try {
            return StringUtils.toString(this.byteBuffer, this.position, expectedLength, encoding);
        } catch (UnsupportedEncodingException uEE) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.1") + encoding + "'", SQLError.SQL_STATE_ILLEGAL_ARGUMENT,
                    exceptionInterceptor);
        } finally {
            this.position += expectedLength; // update cursor
        }
    }

    public void setBufLength(int bufLengthToSet) {
        this.bufLength = bufLengthToSet;
    }

    /**
     * Sets the array of bytes to use as a buffer to read from.
     *
     * @param byteBufferToSet
     *            the array of bytes to use as a buffer
     */
    public void setByteBuffer(byte[] byteBufferToSet) {
        this.byteBuffer = byteBufferToSet;
    }

    /**
     * Set the current position to write to/ read from
     *
     * @param positionToSet
     *            the position (0-based index)
     */
    public void setPosition(int positionToSet) {
        this.position = positionToSet;
    }

    /**
     * Sets whether this packet was part of a multipacket
     *
     * @param flag
     *            was this packet part of a multipacket?
     */
    public void setWasMultiPacket(boolean flag) {
        this.wasMultiPacket = flag;
    }

    @Override
    public String toString() {
        return dumpClampedBytes(getPosition());
    }

    public String toSuperString() {
        return super.toString();
    }

    /**
     * Was this packet part of a multipacket?
     *
     * @return was this packet part of a multipacket?
     */
    public boolean wasMultiPacket() {
        return this.wasMultiPacket;
    }

    public final void writeByte(byte b) throws SQLException {
        ensureCapacity(1);

        this.byteBuffer[this.position++] = b;
    }

    // Write a byte array
    public final void writeBytesNoNull(byte[] bytes) throws SQLException {
        int len = bytes.length;
        ensureCapacity(len);
        System.arraycopy(bytes, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    // Write a byte array with the given offset and length
    public final void writeBytesNoNull(byte[] bytes, int offset, int length) throws SQLException {
        ensureCapacity(length);
        System.arraycopy(bytes, offset, this.byteBuffer, this.position, length);
        this.position += length;
    }

    public final void writeDouble(double d) throws SQLException {
        long l = Double.doubleToLongBits(d);
        writeLongLong(l);
    }

    public final void writeFieldLength(long length) throws SQLException {
        if (length < 251) {
            writeByte((byte) length);
        } else if (length < 65536L) {
            ensureCapacity(3);
            writeByte((byte) 252);
            writeInt((int) length);
        } else if (length < 16777216L) {
            ensureCapacity(4);
            writeByte((byte) 253);
            writeLongInt((int) length);
        } else {
            ensureCapacity(9);
            writeByte((byte) 254);
            writeLongLong(length);
        }
    }

    public final void writeFloat(float f) throws SQLException {
        ensureCapacity(4);

        int i = Float.floatToIntBits(f);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
    }

    public final void writeInt(int i) throws SQLException {
        ensureCapacity(2);

        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
    }

    // Write a String using the specified character encoding
    public final void writeLenBytes(byte[] b) throws SQLException {
        int len = b.length;
        ensureCapacity(len + 9);
        writeFieldLength(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    // Write a String using the specified character encoding
    public final void writeLenString(String s, String encoding, String serverEncoding, SingleByteCharsetConverter converter, boolean parserKnowsUnicode,
                              MySQLConnection conn) throws UnsupportedEncodingException, SQLException {
        byte[] b = null;

        if (converter != null) {
            b = converter.toBytes(s);
        } else {
            b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn, conn.getExceptionInterceptor());
        }

        int len = b.length;
        ensureCapacity(len + 9);
        writeFieldLength(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    public final void writeLong(long i) throws SQLException {
        ensureCapacity(4);

        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
    }

    public final void writeLongInt(int i) throws SQLException {
        ensureCapacity(3);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
    }

    public final void writeLongLong(long i) throws SQLException {
        ensureCapacity(8);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
        b[this.position++] = (byte) (i >>> 32);
        b[this.position++] = (byte) (i >>> 40);
        b[this.position++] = (byte) (i >>> 48);
        b[this.position++] = (byte) (i >>> 56);
    }

    // Write null-terminated string
    public final void writeString(String s) throws SQLException {
        ensureCapacity((s.length() * 3) + 1);
        writeStringNoNull(s);
        this.byteBuffer[this.position++] = 0;
    }

    //	 Write null-terminated string in the given encoding
    public final void writeString(String s, String encoding, MySQLConnection conn) throws SQLException {
        ensureCapacity((s.length() * 3) + 1);
        try {
            writeStringNoNull(s, encoding, encoding, false, conn);
        } catch (UnsupportedEncodingException ue) {
            throw new SQLException(ue.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
        }

        this.byteBuffer[this.position++] = 0;
    }

    // Write string, with no termination
    public final void writeStringNoNull(String s) throws SQLException {
        int len = s.length();
        ensureCapacity(len * 3);
        System.arraycopy(StringUtils.getBytes(s), 0, this.byteBuffer, this.position, len);
        this.position += len;

        // for (int i = 0; i < len; i++)
        // {
        // this.byteBuffer[this.position++] = (byte)s.charAt(i);
        // }
    }

    // Write a String using the specified character encoding
    public final void writeStringNoNull(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn)
            throws UnsupportedEncodingException, SQLException {
        byte[] b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn, conn.getExceptionInterceptor());

        int len = b.length;
        ensureCapacity(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }
}
