package cn.fxbin.bubble.core.util.time;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * TimeConverter
 * 时间转换工具类
 * 提供多种时间类型转换为时间戳的功能
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/10/17 10:50
 */
@UtilityClass
public class TimeConverter {


    /**
     * 将时间值转换为时间戳
     *
     * @param value 时间值，支持多种类型
     * @return 时间戳（毫秒）
     * @throws IllegalArgumentException 当输入类型不支持或格式错误时抛出
     */
    public Long convertToTimestamp(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof LocalDateTime) {
            return DateUtils.toEpochMilli((LocalDateTime) value);
        }

        if (value instanceof LocalDate) {
            return DateUtils.toEpochMilli((LocalDate) value);
        }

        if (value instanceof Date) {
            return DateUtils.toEpochMilli((Date) value);
        }

        if (value instanceof Instant) {
            return DateUtils.toEpochMilli((Instant) value);
        }

        if (value instanceof ZonedDateTime) {
            return DateUtils.toEpochMilli((ZonedDateTime) value);
        }

        if (value instanceof String) {
            return parseStringToTimestamp((String) value);
        }

        throw new IllegalArgumentException("不支持的时间类型: " + value.getClass().getName());
    }

    /**
     * 解析字符串为时间戳
     *
     * @param value 时间字符串
     * @return 时间戳（毫秒）
     * @throws IllegalArgumentException 当字符串格式无法解析时抛出
     */
    private Long parseStringToTimestamp(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }

        try {
            // 尝试解析为Long类型的时间戳
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 不是纯数字，尝试解析为日期时间格式
            try {
                // 尝试使用hutool的DateUtil解析
                Date date = DateUtil.parse(value);
                return date.getTime();
            } catch (Exception ex) {
                // 尝试使用Java 8的DateTimeFormatter解析常见格式
                return tryParseWithCommonFormats(value);
            }
        }
    }

    /**
     * 尝试使用常见格式解析日期时间字符串
     *
     * @param value 日期时间字符串
     * @return 时间戳（毫秒）
     * @throws IllegalArgumentException 当所有格式都无法解析时抛出
     */
    private Long tryParseWithCommonFormats(String value) {
        // 常见的日期时间格式
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.contains("HH:mm")) {
                    LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                    return DateUtils.toEpochMilli(dateTime);
                } else {
                    LocalDate date = LocalDate.parse(value, formatter);
                    return DateUtils.toEpochMilli(date);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }

        throw new IllegalArgumentException("无法解析的日期时间格式: " + value);
    }

}
