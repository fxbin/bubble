package cn.fxbin.bubble.fireworks.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * JsonUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 17:28
 */
@UtilityClass
public class JsonUtils {


    /**
     * toJson 将对象序列化成json字符串儿
     *
     * @author fxbin
     * @since 2020/3/20 17:30
     * @param object jsonBean
     * @return java.lang.String
     */
    public String toJson(Object object) {
        try {
            return getInstance().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * isJsonString 是否为json格式字符串
     *
     * @author fxbin
     * @since 2020/3/20 17:31
     * @param jsonString 字符串
     * @return boolean
     */
    public boolean isJsonString(String jsonString) {
        try {
            getInstance().readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * isJsonSerialize 是否可以json序列化
     *
     * @author fxbin
     * @since 2020/3/20 17:33
     * @param object object
     * @return boolean
     */
    public boolean isJsonSerialize(Object object) {
        try {
            getInstance().writeValueAsBytes(object);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }


    /**
     * parse 将json反序列化成对象
     *
     * @author fxbin
     * @since 2020/3/20 17:33
     * @param jsonString json 字符串
     * @param requiredType type the bean must match; can be an interface or superclass
     * @return T
     */
    public <T> T parse(String jsonString, Class<T> requiredType) {
        try {
            return getInstance().readValue(jsonString, requiredType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getInstance 获取jackson实例
     *
     * @author fxbin
     * @since 2020/3/20 17:30
     * @return com.fasterxml.jackson.databind.ObjectMapper
     */
    private static ObjectMapper getInstance() {
        return JacksonHolder.INSTANCE;
    }


    private static class JacksonHolder {
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }

}
