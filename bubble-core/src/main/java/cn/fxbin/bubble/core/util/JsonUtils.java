package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.dataobject.GlobalErrorCode;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.exception.UtilException;
import cn.fxbin.bubble.core.module.JacksonHolder;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 提取
     *
     * @param jsonStr   json str
     * @param fieldName 字段名
     * @param delim     delim
     * @return {@link String}
     */
    public String extract(String jsonStr, @NonNull String fieldName, String delim) {
        List<String> list = JsonUtils.extract(jsonStr, fieldName);
        return StringUtils.join(list, delim);
    }

    /**
     * 提取字段数据
     *
     * @param jsonStr   json str
     * @param fieldName 字段名
     * @return {@link List}<{@link String}>
     */
    public List<String> extract(String jsonStr, @NonNull String fieldName) {

        if (StringUtils.isNotBlank(jsonStr) && JsonUtils.isJsonString(jsonStr)) {
            List<Map<String, Object>> mapList;
            try {
                mapList = JsonUtils.parse(jsonStr, new TypeReference<>() {
                });
            } catch (Exception e) {
                throw new ServiceException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
            }

            return mapList.stream().map(m -> StringUtils.utf8Str(m.get(fieldName))).collect(Collectors.toList());
        }
        return Lists.newArrayList();
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

    /**
     * parse
     *
     * @param content json content
     * @param valueTypeRef TypeReference
     * @return {@link T}
     */
    public <T> T parse(String content, TypeReference<T> valueTypeRef) {
        try {
            return JacksonHolder.INSTANCE.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

}
