package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.exception.UtilException;
import cn.fxbin.bubble.core.module.JacksonHolder;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class JsonUtils extends JSONUtil {


    /**
     * toJson 将对象序列化成json字符串儿
     *
     * @since 2020/3/20 17:30
     * @param object jsonBean
     * @return java.lang.String
     */
    public String toJson(Object object) {
        try {
            if (ObjectUtils.isEmpty(object)) {
                return null;
            }
            if (object instanceof CharSequence) {
                return StringUtils.utf8Str(object);
            }
            return JacksonHolder.INSTANCE.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UtilException(e);
        }
    }

    /**
     * json字节
     *
     * @param value jsonBean
     * @return {@link byte[]}
     */
    public byte[] toJsonByte(Object value) {
        try {
            return JacksonHolder.INSTANCE.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new UtilException(e);
        }
    }


    /**
     * isJsonString 是否为json格式字符串
     *
     * @since 2020/3/20 17:31
     * @param jsonString 字符串
     * @return boolean
     */
    public boolean isJsonString(String jsonString) {
        try {
            JacksonHolder.INSTANCE.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * isJsonSerialize 是否可以json序列化
     *
     * @since 2020/3/20 17:33
     * @param object object
     * @return boolean
     */
    public boolean isJsonSerialize(Object object) {
        try {
            JacksonHolder.INSTANCE.writeValueAsBytes(object);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }


    /**
     * parse 将json反序列化成对象
     *
     * @since 2020/3/20 17:33
     * @param jsonString json 字符串
     * @param requiredType type the bean must match; can be an interface or superclass
     * @return T
     */
    public <T> T parse(String jsonString, Class<T> requiredType) {
        try {
            return JacksonHolder.INSTANCE.readValue(jsonString, requiredType);
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

}
