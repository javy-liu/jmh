package org.oyach.jmh.ser;


import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/9
 * @since 0.0.1
 */
public class PSUtil {

    public static byte[] obj2byte(Object object){
        Schema schema = RuntimeSchema.getSchema(object.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(4096);

        return ProtobufIOUtil.toByteArray(object, schema, buffer);

//        ProtostuffIOUtil.toByteArray(object, schema, buffer);
    }

    public static <T> T byte2obj(byte[] bytes, Class<T> clazz){
        T t = null;
        try {
            t = clazz.newInstance();
            Schema schema = RuntimeSchema.getSchema(clazz);
            ProtobufIOUtil.mergeFrom(bytes, t, schema);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }
 }
