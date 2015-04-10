package org.oyach.jmh.ser;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/9
 * @since 0.0.1
 */
public class JsonUtil {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }
    public static String obj2json(Object object){
        return JSONObject.toJSONString(object);
    }


    public static <T> T json2Obj(String json, Class<T> clazz){
        return JSONObject.parseObject(json, clazz);
    }


    public static String obj2json2(Object object){

        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static <T> T json2Obj2(String json, Class<T> clazz){
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
