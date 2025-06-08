package cn.fxbin.bubble.core.module;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 自定义 Java 时间模块 - 优化版本
 * 支持中国标准时间格式，提供统一的时间序列化和反序列化策略
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/20 14:54
 */
public class JavaTimeModule extends SimpleModule {

    private static final long serialVersionUID = 1L;
    
    // 预编译的时间格式化器，提升性能
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN);

    public JavaTimeModule() {
        super("CustomJavaTimeModule", PackageVersion.VERSION);
        
        // 序列化器配置
        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
        
        // 反序列化器配置
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
        addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
    }
}
