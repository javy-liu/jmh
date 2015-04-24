package org.oyach.jmh.server;

import com.mysql.jdbc.*;
import org.junit.Test;
import org.oyach.jmh.server.mysql.MysqlProxyServer;
import org.oyach.jmh.server.mysql.jdbc.Buffer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Deflater;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/21
 * @since 0.0.1
 */
public class ProxyServerTest {


    private static final String CODE_PAGE_1252 = "Cp1252";
    protected static final int NULL_LENGTH = ~0;
    protected static final int COMP_HEADER_LENGTH = 3;
    protected static final int MIN_COMPRESS_LEN = 50;
    protected static final int HEADER_LENGTH = 4;
    protected static final int AUTH_411_OVERHEAD = 33;
    private static int maxBufferSize = 65535;

    private static final String NONE = "none";

    private static final int CLIENT_LONG_PASSWORD = 0x00000001; /* new more secure passwords */
    private static final int CLIENT_FOUND_ROWS = 0x00000002;
    private static final int CLIENT_LONG_FLAG = 0x00000004; /* Get all column flags */
    protected static final int CLIENT_CONNECT_WITH_DB = 0x00000008;
    private static final int CLIENT_COMPRESS = 0x00000020; /* Can use compression protcol */
    private static final int CLIENT_LOCAL_FILES = 0x00000080; /* Can use LOAD DATA LOCAL */
    private static final int CLIENT_PROTOCOL_41 = 0x00000200; // for > 4.1.1
    private static final int CLIENT_INTERACTIVE = 0x00000400;
    protected static final int CLIENT_SSL = 0x00000800;
    private static final int CLIENT_TRANSACTIONS = 0x00002000; // Client knows about transactions
    protected static final int CLIENT_RESERVED = 0x00004000; // for 4.1.0 only
    protected static final int CLIENT_SECURE_CONNECTION = 0x00008000;
    private static final int CLIENT_MULTI_STATEMENTS = 0x00010000; // Enable/disable multiquery support
    private static final int CLIENT_MULTI_RESULTS = 0x00020000; // Enable/disable multi-results
    private static final int CLIENT_PLUGIN_AUTH = 0x00080000;
    private static final int CLIENT_CONNECT_ATTRS = 0x00100000;
    private static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;
    private static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORD = 0x00400000;

    private static final int SERVER_STATUS_IN_TRANS = 1;
    private static final int SERVER_STATUS_AUTOCOMMIT = 2; // Server in auto_commit mode
    static final int SERVER_MORE_RESULTS_EXISTS = 8; // Multi query - next query exists
    private static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
    private static final int SERVER_QUERY_NO_INDEX_USED = 32;
    private static final int SERVER_QUERY_WAS_SLOW = 2048;
    private static final int SERVER_STATUS_CURSOR_EXISTS = 64;
    private static final String FALSE_SCRAMBLE = "xxxxxxxx";
    protected static final int MAX_QUERY_SIZE_TO_LOG = 1024; // truncate logging of queries at 1K
    protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 1024 * 1024; // don't explain queries above 1MB
    protected static final int INITIAL_PACKET_SIZE = 1024;


    /**
     * We store the platform 'encoding' here, only used to avoid munging filenames for LOAD DATA LOCAL INFILE...
     */
    private static String jvmPlatformCharset = null;

    /**
     * We need to have a 'marker' for all-zero datetimes so that ResultSet can decide what to do based on connection setting
     */
    protected final static String ZERO_DATE_VALUE_MARKER = "0000-00-00";
    protected final static String ZERO_DATETIME_VALUE_MARKER = "0000-00-00 00:00:00";

    private static final String EXPLAINABLE_STATEMENT = "SELECT";
    private static final String[] EXPLAINABLE_STATEMENT_EXTENSION = new String[] { "INSERT", "UPDATE", "REPLACE", "DELETE" };




    /** Max number of bytes to dump when tracing the protocol */
    private final static int MAX_PACKET_DUMP_LENGTH = 1024;
    private boolean packetSequenceReset = false;
    protected int serverCharsetIndex;

    //
    // Use this when reading in rows to avoid thousands of new() calls, because the byte arrays just get copied out of the packet anyway
    //
    private com.mysql.jdbc.Buffer reusablePacket = null;
    private com.mysql.jdbc.Buffer sendPacket = null;
    private com.mysql.jdbc.Buffer sharedSendPacket = null;

    /** Data to the server */
    protected BufferedOutputStream mysqlOutput = null;
    protected MySQLConnection connection;
    private Deflater deflater = null;
    protected InputStream mysqlInput = null;
    private LinkedList<StringBuilder> packetDebugRingBuffer = null;
    private RowData streamingData = null;

    /** The connection to the server */
    public Socket mysqlConnection = null;
    protected SocketFactory socketFactory = null;

    //
    // Packet used for 'LOAD DATA LOCAL INFILE'
    //
    // We use a SoftReference, so that we don't penalize intermittent use of this feature
    //
    private SoftReference<com.mysql.jdbc.Buffer> loadFileBufRef;

    //
    // Used to send large packets to the server versions 4+
    // We use a SoftReference, so that we don't penalize intermittent use of this feature
    //
    private SoftReference<com.mysql.jdbc.Buffer> splitBufRef;
    private SoftReference<com.mysql.jdbc.Buffer> compressBufRef;
    protected String host = null;
    protected String seed;
    private String serverVersion = null;
    private String socketFactoryClassName = null;
    private byte[] packetHeaderBuf = new byte[4];
    private boolean colDecimalNeedsBump = false; // do we need to increment the colDecimal flag?
    private boolean hadWarnings = false;
    private boolean has41NewNewProt = false;

    /** Does the server support long column info? */
    private boolean hasLongColumnInfo = false;
    private boolean isInteractiveClient = false;
    private boolean logSlowQueries = false;

    /**
     * Does the character set of this connection match the character set of the
     * platform
     */
    private boolean platformDbCharsetMatches = true; // changed once we've connected.
    private boolean profileSql = false;
    private boolean queryBadIndexUsed = false;
    private boolean queryNoIndexUsed = false;
    private boolean serverQueryWasSlow = false;

    /** Should we use 4.1 protocol extensions? */
    private boolean use41Extensions = false;
    private boolean useCompression = false;
    private boolean useNewLargePackets = false;
    private boolean useNewUpdateCounts = false; // should we use the new larger update counts?
    private byte packetSequence = 0;
    private byte compressedPacketSequence = 0;
    private byte readPacketSequence = -1;
    private boolean checkPacketSequence = false;
    private byte protocolVersion = 0;
    private int maxAllowedPacket = 1024 * 1024;
    protected int maxThreeBytes = 255 * 255 * 255;
    protected int port = 3306;
    protected int serverCapabilities;
    private int serverMajorVersion = 0;
    private int serverMinorVersion = 0;
    private int oldServerStatus = 0;
    private int serverStatus = 0;
    private int serverSubMinorVersion = 0;
    private int warningCount = 0;
    protected long clientParam = 0;
    protected long lastPacketSentTimeMs = 0;
    protected long lastPacketReceivedTimeMs = 0;
    private boolean traceProtocol = false;
    private boolean enablePacketDebug = false;
    private boolean useConnectWithDb;
    private boolean needToGrabQueryFromPacket;
    private boolean autoGenerateTestcaseScript;
    private long threadId;
    private boolean useNanosForElapsedTime;
    private long slowQueryThreshold;
    private String queryTimingUnits;
    private boolean useDirectRowUnpack = true;
    private int useBufferRowSizeThreshold;
    private int commandCount = 0;
    private List<StatementInterceptorV2> statementInterceptors;
    private ExceptionInterceptor exceptionInterceptor;
    private int authPluginDataLength = 0;

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3307/ceit";
    static final String DB_URL2 = "jdbc:mysql://localhost:3306/mybatis";
    static final String DB_URL3 = "jdbc:mysql://127.0.0.1:5050/mybatis";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "root";


    @Test
    public void test01() throws Exception {

        MysqlProxyServer mysqlProxyServer = new MysqlProxyServer();

        new Thread(mysqlProxyServer).start();

        Thread.sleep(1000 * 60 * 60);
    }

    @Test
    public void test02() throws Exception {


        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            String sql = "SELECT id, first, last, age FROM student";
            ResultSet rs = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int id  = rs.getInt("id");
                int age = rs.getInt("age");
                String first = rs.getString("first");
                String last = rs.getString("last");

                //Display values
                System.out.print("ID: " + id);
                System.out.print(", Age: " + age);
                System.out.print(", First: " + first);
                System.out.println(", Last: " + last);
            }
            rs.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");

    }

    @Test
    public void test03() throws Exception {


        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL3, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            String sql = "SELECT id, username FROM user";
            ResultSet rs = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int id  = rs.getInt("id");
                String username = rs.getString("username");

                //Display values
                System.out.print("ID: " + id);
                System.out.print(", username: " + username);

            }
            rs.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");

    }



    @Test
    public void test04() throws Exception {
        int max = 1048576;

        byte[] bytes = new byte[]{11,22,33,44};
        System.out.println((3 & 255) << 8);
        System.out.println((0 & 255) << 16);

        byte[] bytes1 = ByteArrayUtil.toBytes(78);

        System.out.println();
    }

    @Test
    public void test05() throws Exception {

        byte[] packetHeaderBuf = new byte[]{74, 0, 0, 0};

        byte[] bytes = new byte[]{ 10, // protocolVersion  byte
                53, 46, 54, 46, 50, 51, 0, // version    string 需要0来表示结束
                10, 0, 0, 0, // threadId  long
                40, 55, 123, 41, 87, 91, 69,
                38, 0, -1, -9, 33, 2, 0, 127, -128, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 69, 90, 117, 96, 93, 53, 48,
                46, 53, 96, 102, 0, 109, 121, 115, 113, 108, 95, 110, 97, 116, 105, 118, 101, 95, 112, 97, 115,
                115, 119, 111, 114, 100, 0};

        Buffer buf = new Buffer(bytes);




        // Get the protocol version
        this.protocolVersion = buf.readByte();


        this.serverVersion = buf.readString("ASCII", getExceptionInterceptor());

        // Parse the server version into major/minor/subminor
        int point = this.serverVersion.indexOf('.');

        if (point != -1) {
            try {
                int n = Integer.parseInt(this.serverVersion.substring(0, point));
                this.serverMajorVersion = n;
            } catch (NumberFormatException NFE1) {
                // ignore
            }

            String remaining = this.serverVersion.substring(point + 1, this.serverVersion.length());
            point = remaining.indexOf('.');

            if (point != -1) {
                try {
                    int n = Integer.parseInt(remaining.substring(0, point));
                    this.serverMinorVersion = n;
                } catch (NumberFormatException nfe) {
                    // ignore
                }

                remaining = remaining.substring(point + 1, remaining.length());

                int pos = 0;

                while (pos < remaining.length()) {
                    if ((remaining.charAt(pos) < '0') || (remaining.charAt(pos) > '9')) {
                        break;
                    }

                    pos++;
                }

                try {
                    int n = Integer.parseInt(remaining.substring(0, pos));
                    this.serverSubMinorVersion = n;
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }

        if (versionMeetsMinimum(4, 0, 8)) {
            this.maxThreeBytes = (256 * 256 * 256) - 1;
            this.useNewLargePackets = true;
        } else {
            this.maxThreeBytes = 255 * 255 * 255;
            this.useNewLargePackets = false;
        }

        this.colDecimalNeedsBump = versionMeetsMinimum(3, 23, 0);
        this.colDecimalNeedsBump = !versionMeetsMinimum(3, 23, 15); // guess? Not noted in changelog
        this.useNewUpdateCounts = versionMeetsMinimum(3, 22, 5);

        // read connection id
        this.threadId = buf.readLong();

        if (this.protocolVersion > 9) {
            // read auth-plugin-data-part-1 (string[8])
            this.seed = buf.readString("ASCII", getExceptionInterceptor(), 8);
            // read filler ([00])
            buf.readByte();
        } else {
            // read scramble (string[NUL])
            this.seed = buf.readString("ASCII", getExceptionInterceptor());
        }

        this.serverCapabilities = 0;

        // read capability flags (lower 2 bytes)
        if (buf.getPosition() < buf.getBufLength()) {
            this.serverCapabilities = buf.readInt();
        }

        if ((versionMeetsMinimum(4, 1, 1) || ((this.protocolVersion > 9) && (this.serverCapabilities & CLIENT_PROTOCOL_41) != 0))) {

            /* New protocol with 16 bytes to describe server characteristics */
            // read character set (1 byte)
            this.serverCharsetIndex = buf.readByte() & 0xff;
            // read status flags (2 bytes)
            this.serverStatus = buf.readInt();
            checkTransactionState(0);

            // read capability flags (upper 2 bytes)
            this.serverCapabilities |= buf.readInt() << 16;

            if ((this.serverCapabilities & CLIENT_PLUGIN_AUTH) != 0) {
                // read length of auth-plugin-data (1 byte)
                this.authPluginDataLength = buf.readByte() & 0xff;
            } else {
                // read filler ([00])
                buf.readByte();
            }
            // next 10 bytes are reserved (all [00])
            buf.setPosition(buf.getPosition() + 10);

            if ((this.serverCapabilities & CLIENT_SECURE_CONNECTION) != 0) {
                String seedPart2;
                StringBuilder newSeed;
                // read string[$len] auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
                if (this.authPluginDataLength > 0) {
                    // TODO: disabled the following check for further clarification
                    //         			if (this.authPluginDataLength < 21) {
                    //                      forceClose();
                    //                      throw SQLError.createSQLException(Messages.getString("MysqlIO.103"),
                    //                          SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, getExceptionInterceptor());
                    //         			}
                    seedPart2 = buf.readString("ASCII", getExceptionInterceptor(), this.authPluginDataLength - 8);
                    newSeed = new StringBuilder(this.authPluginDataLength);
                } else {
                    seedPart2 = buf.readString("ASCII", getExceptionInterceptor());
                    newSeed = new StringBuilder(20);
                }
                newSeed.append(this.seed);
                newSeed.append(seedPart2);
                this.seed = newSeed.toString();
            }
        }


        if (((this.serverCapabilities & CLIENT_COMPRESS) != 0) && false) {
            this.clientParam |= CLIENT_COMPRESS;
        }

        this.useConnectWithDb = true; // 直接指定有数据库

        if (this.useConnectWithDb) {
            this.clientParam |= CLIENT_CONNECT_WITH_DB;
        }

        if (((this.serverCapabilities & CLIENT_SSL) == 0) && this.connection.getUseSSL()) {
            if (this.connection.getRequireSSL()) {
                this.connection.close();
                throw SQLError.createSQLException(Messages.getString("MysqlIO.15"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE,
                        getExceptionInterceptor());
            }

            this.connection.setUseSSL(false);
        }

        if ((this.serverCapabilities & CLIENT_LONG_FLAG) != 0) {
            // We understand other column flags, as well
            this.clientParam |= CLIENT_LONG_FLAG;
            this.hasLongColumnInfo = true;
        }

        // return FOUND rows
        if (!this.connection.getUseAffectedRows()) {
            this.clientParam |= CLIENT_FOUND_ROWS;
        }

        if (this.connection.getAllowLoadLocalInfile()) {
            this.clientParam |= CLIENT_LOCAL_FILES;
        }

        if (this.isInteractiveClient) {
            this.clientParam |= CLIENT_INTERACTIVE;
        }

        //
        // switch to pluggable authentication if available
        //
        if ((this.serverCapabilities & CLIENT_PLUGIN_AUTH) != 0) {
//            proceedHandshakeWithPluggableAuthentication(user, password, database, buf);
            return;
        }

        // Authenticate
        if (this.protocolVersion > 9) {
            this.clientParam |= CLIENT_LONG_PASSWORD; // for long passwords
        } else {
            this.clientParam &= ~CLIENT_LONG_PASSWORD;
        }



        String str = new String(bytes);

        byte[] bytes1 = new byte[]{0, 0, 0, 2, 0, 0, 0};
        String str1 =  new String(bytes1);

        byte[] bytes2 = new byte[]{7, 0, 0, 2};
        String str2 =  new String(bytes2);
        System.out.println();
    }


    boolean versionMeetsMinimum(int major, int minor, int subminor) {
        if (getServerMajorVersion() >= major) {
            if (getServerMajorVersion() == major) {
                if (getServerMinorVersion() >= minor) {
                    if (getServerMinorVersion() == minor) {
                        return (getServerSubMinorVersion() >= subminor);
                    }

                    // newer than major.minor
                    return true;
                }

                // older than major.minor
                return false;
            }

            // newer than major
            return true;
        }

        return false;
    }

    public int getServerMajorVersion() {
        return serverMajorVersion;
    }

    public void setServerMajorVersion(int serverMajorVersion) {
        this.serverMajorVersion = serverMajorVersion;
    }

    public int getServerMinorVersion() {
        return serverMinorVersion;
    }

    public void setServerMinorVersion(int serverMinorVersion) {
        this.serverMinorVersion = serverMinorVersion;
    }

    public int getServerSubMinorVersion() {
        return serverSubMinorVersion;
    }

    public void setServerSubMinorVersion(int serverSubMinorVersion) {
        this.serverSubMinorVersion = serverSubMinorVersion;
    }


    private void checkTransactionState(int oldStatus) throws SQLException {
        boolean previouslyInTrans = ((oldStatus & SERVER_STATUS_IN_TRANS) != 0);
        boolean currentlyInTrans = ((this.serverStatus & SERVER_STATUS_IN_TRANS) != 0);

        if (previouslyInTrans && !currentlyInTrans) {
            this.connection.transactionCompleted();
        } else if (!previouslyInTrans && currentlyInTrans) {
            this.connection.transactionBegun();
        }
    }

    public ExceptionInterceptor getExceptionInterceptor() {
        return exceptionInterceptor;
    }
}