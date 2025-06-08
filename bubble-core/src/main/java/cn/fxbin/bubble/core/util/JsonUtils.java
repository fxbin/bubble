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
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JsonUtils - 优化版本
 * 提供高性能、线程安全的 JSON 序列化和反序列化工具
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 17:28
 */
@Slf4j
@UtilityClass
public class JsonUtils extends JSONUtil {


    /**
     * toJson 将对象序列化成json字符串
     *
     * @since 2020/3/20 17:30
     * @param object jsonBean
     * @return java.lang.String
     * @throws UtilException 序列化失败时抛出
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
            log.error("JSON 序列化失败，对象类型: {}", object != null ? object.getClass().getSimpleName() : "null", e);
            throw new UtilException("JSON 序列化失败", e);
        }
    }

    /**
     * toJsonByte 将对象序列化成json字节数组
     *
     * @param value jsonBean
     * @return {@link byte[]}
     * @throws UtilException 序列化失败时抛出
     */
    public byte[] toJsonByte(Object value) {
        try {
            return JacksonHolder.INSTANCE.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            log.error("JSON 字节序列化失败，对象类型: {}", value != null ? value.getClass().getSimpleName() : "null", e);
            throw new UtilException("JSON 字节序列化失败", e);
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
     * @return {@link List<String>}
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
     * @throws UtilException 反序列化失败时抛出
     */
    public <T> T parse(String jsonString, Class<T> requiredType) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            return JacksonHolder.INSTANCE.readValue(jsonString, requiredType);
        } catch (IOException e) {
            log.error("JSON 反序列化失败，目标类型: {}, JSON内容: {}", requiredType.getSimpleName(), 
                     StringUtils.isNotBlank(jsonString) && jsonString.length() > 200 ? 
                     jsonString.substring(0, 200) + "..." : jsonString, e);
            throw new UtilException("JSON 反序列化失败", e);
        }
    }

    /**
     * parse 将json反序列化成对象（支持泛型）
     *
     * @param content json content
     * @param valueTypeRef TypeReference
     * @return {@link T}
     * @throws UtilException 反序列化失败时抛出
     */
    public <T> T parse(String content, TypeReference<T> valueTypeRef) {
        try {
            if (StringUtils.isBlank(content)) {
                return null;
            }
            return JacksonHolder.INSTANCE.readValue(content, valueTypeRef);
        } catch (IOException e) {
            log.error("JSON 反序列化失败，目标类型: {}, JSON内容: {}", valueTypeRef.getType().getTypeName(), 
                     StringUtils.isNotBlank(content) && content.length() > 200 ? 
                     content.substring(0, 200) + "..." : content, e);
            throw new UtilException("JSON 反序列化失败", e);
        }
    }

}
