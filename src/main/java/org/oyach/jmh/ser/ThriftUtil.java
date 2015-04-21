package org.oyach.jmh.ser;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/13
 * @since 0.0.1
 */
public class ThriftUtil {


    public static byte[] obj2byte(TBase tBase) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TTransport trans = new TIOStreamTransport(out);

            TProtocol tProtocol = new TCompactProtocol(trans);
            tBase.write(tProtocol);

            return out.toByteArray();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends TBase> T byte2obj(byte[] bytes, Class<T> clazz) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            TTransport trans = new TIOStreamTransport(in);
            TProtocol tProtocol = new TCompactProtocol(trans);

            T t = clazz.newInstance();

            t.read(tProtocol);

            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }
}
