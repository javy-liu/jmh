package org.oyach.jmh.ser;

import java.io.*;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/9
 * @since 0.0.1
 */
public class JavaSerUtil {

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream b = null;
        try {
            b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b.toByteArray();
    }

    public static Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            return o.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}
