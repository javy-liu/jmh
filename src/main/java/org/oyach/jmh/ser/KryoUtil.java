package org.oyach.jmh.ser;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/9
 * @since 0.0.1
 */
public class KryoUtil {
    private Kryo kryo;

    {
        kryo = new Kryo();
    }


    public byte[] object2byte(Object object){
        ByteBufferOutput byteBufferOutput = new ByteBufferOutput(4096);
        kryo.writeObject(byteBufferOutput, object);
        return byteBufferOutput.toBytes();
    }

    public <T> T byte2object(byte[] bytes, Class<T> type){
        ByteBufferInput byteBufferInput = new ByteBufferInput(bytes);
        return kryo.readObject(byteBufferInput, type);
    }
}
