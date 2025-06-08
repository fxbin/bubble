package cn.fxbin.bubble.core.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.TimeZone;

/**
 * JacksonHolder - 优化版本
 * 提供全局统一的 ObjectMapper 实例，配置完善的序列化和反序列化策略
 * 
 * 修复说明：
 * - 添加了标准 JSR310 模块支持，解决 Duration、Period 等 Java 8 时间类型序列化问题
 * - 保留自定义时间模块，提供中国标准时间格式支持
 * - 确保完整的 Java 8 时间 API 兼容性
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/20 15:18
 */
public interface JacksonHolder {

    ObjectMapper INSTANCE = createObjectMapper();

    /**
     * 创建并配置 ObjectMapper 实例
     * 
     * @return 配置完成的 ObjectMapper
     */
    static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册标准 JSR310 模块（支持所有 Java 8 时间类型，包括 Duration、Period 等）
        mapper.registerModule(new JavaTimeModule());
        
        // 注册自定义时间模块（包含中国标准时间格式）
        mapper.registerModule(new cn.fxbin.bubble.core.module.JavaTimeModule());
        
        // 注册 NullValue 序列化器（用于 Spring Cache）
        mapper.registerModule(new NullValueModule());
        
        // 配置序列化特性
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        
        // 配置反序列化特性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        
        // 配置生成器特性
        mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        
        // 设置时区为中国标准时间
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 设置序列化包含策略（排除 null 值）
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return mapper;
    }

    /**
     * NullValue 专用模块
     */
    class NullValueModule extends SimpleModule {
        
        private static final long serialVersionUID = 1L;
        
        public NullValueModule() {
            super("NullValueModule");
            addSerializer(NullValue.class, new NullValueSerializer(null));
        }
    }

    /**
     * {@link StdSerializer} adding class information required by default typing. This allows de-/serialization of
     * {@link NullValue}.
     *
     * @author Christoph Strobl
     * @since 1.8
     */
    class NullValueSerializer extends StdSerializer<NullValue> {

        private static final long serialVersionUID = 1999052150548658808L;
        private final String classIdentifier;

        /**
         * @param classIdentifier can be {@literal null} and will be defaulted to {@code @class}.
         */
        NullValueSerializer(@Nullable String classIdentifier) {

            super(NullValue.class);
            this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {

            jgen.writeStartObject();
            jgen.writeStringField(classIdentifier, NullValue.class.getName());
            jgen.writeEndObject();
        }
    }

}


