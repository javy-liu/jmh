package org.oyach.jmh.ser;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import org.oyach.jmh.vo.TeacherProtos;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/22
 * @since 0.0.1
 */
public class PBUtil {

    public static byte[] obj2byte(GeneratedMessage message) {

        return message.toByteArray();
    }

    public static TeacherProtos.Teacher byte2obj(byte[] bytes) throws InvalidProtocolBufferException {

        return TeacherProtos.Teacher.parseFrom(bytes);
    }
}
