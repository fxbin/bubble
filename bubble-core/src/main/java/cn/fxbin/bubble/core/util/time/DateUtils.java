package cn.fxbin.bubble.core.util.time;

import cn.fxbin.bubble.core.exception.UtilException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * DateUtils - 优化版本
 * 提供高性能的日期时间工具，包含DateTimeFormatter缓存优化
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:09
 */
@Slf4j
@UtilityClass
public class DateUtils {

    /**
     * DateTimeFormatter缓存，提升性能
     */
    private final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 获取缓存的DateTimeFormatter
     * 
     * @param pattern 日期格式模式
     * @return DateTimeFormatter
     */
    private DateTimeFormatter getCachedFormatter(String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

    // --------- norm date pattern

    /** 标准日期格式：yyyy-MM-dd */
    public final String NORM_DATE_PATTERN = "yyyy-MM-dd";

    /** 国际标准日期格式：dd-MM-yyyy  */
    public final String NORM_I18N_DATE_PATTERN = "dd-MM-yyyy";

    /** 标准时间格式：HH:mm:ss */
    public final String NORM_TIME_PATTERN = "HH:mm:ss";

    /** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
    public final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 国际标准时间格式， 精确到秒 dd/MM/yyy HH:mm:ss */
    public final String NORM_I18N_DATETIME_FORMAT = "dd/MM/yyy HH:mm:ss";

    /** 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS */
    public final String NORM_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /** ZonedDateTime 时区时间格式 */
    public final String NORM_ZONE_DATA_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

    /** 标准日期格式：yyyy年MM月dd日 */
    public final String CHINESE_DATE_PATTERN = "yyyy年MM月dd日";

    // --------- pure date pattern

    /** 标准日期格式: YYMMdd */
    public final String PURE_SHORT_DATE_PATTERN = "YYMMdd";

    /** 标准日期格式：yyyyMMdd */
    public final String PURE_DATE_PATTERN = "yyyyMMdd";

    /** 标准日期格式：HHmmss */
    public final String PURE_TIME_PATTERN = "HHmmss";

    /** 标准日期格式：yyyyMMddHHmmss */
    public final String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";

    /** 标准日期格式：yyyyMMddHHmmssSSS */
    public final String PURE_DATETIME_MS_PATTERN = "yyyyMMddHHmmssSSS";


    @SuppressWarnings("AlibabaStringConcat")
    public String formatDateText(String dateText) {
        Map<String, String> dateRegExpress = new HashMap<>();
        // 2020年3月23日 11时39分34秒，2020-03-23 11:39:34，2020/3/23 11:39:34
        dateRegExpress.put("^\\d{4}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D*$", "yyyy-MM-dd-HH-mm-ss");
        // 2020-03-23 11:39
        dateRegExpress.put("^\\d{4}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd-HH-mm");
        // 2020-03-23 11
        dateRegExpress.put("^\\d{4}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd-HH");
        // 2020-03-23
        dateRegExpress.put("^\\d{4}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd");
        // 2020-03
        dateRegExpress.put("^\\d{4}\\D+\\d{2}$", "yyyy-MM");
        // 2020
        dateRegExpress.put("^\\d{4}$", "yyyy");
        // 20200323113934
        dateRegExpress.put("^\\d{14}$", "yyyyMMddHHmmss");
        // 202003231139
        dateRegExpress.put("^\\d{12}$", "yyyyMMddHHmm");
        // 2020032311
        dateRegExpress.put("^\\d{10}$", "yyyyMMddHH");
        // 20200323
        dateRegExpress.put("^\\d{8}$", "yyyyMMdd");
        // 202003
        dateRegExpress.put("^\\d{6}$", "yyyyMM");
        // 13:39:34  拼接当前日期
        dateRegExpress.put("^\\d{2}\\s*:\\s*\\d{2}\\s*:\\s*\\d{2}$", "yyyy-MM-dd-HH-mm-ss");
        // 11:39  拼接当前日期
        dateRegExpress.put("^\\d{2}\\s*:\\s*\\d{2}$", "yyyy-MM-dd-HH-mm");
        // 20.03.23(年.月.日)
        dateRegExpress.put("^\\d{2}\\D+\\d{1,2}\\D+\\d{1,2}$", "yy-MM-dd");
        // 23.03(日.月) 拼接当前年份
        dateRegExpress.put("^\\d{1,2}\\D+\\d{1,2}$", "yyyy-dd-MM");
        // 23.03.2020(日.月.年)
        dateRegExpress.put("^\\d{1,2}\\D+\\d{1,2}\\D+\\d{4}$", "dd-MM-yyyy");

        // yyyy-MM-dd
        String curDate = getCurDateOfNormDatePattern();
        SimpleDateFormat formatter = new SimpleDateFormat(NORM_DATETIME_PATTERN);

        String dateReplace;
        SimpleDateFormat timeFormatter;
        String formatDataStr = "";
        try {
            for (String key : dateRegExpress.keySet()) {
                if (Pattern.compile(key).matcher(dateText).matches()) {

                    timeFormatter = new SimpleDateFormat(dateRegExpress.get(key));
                    //13:05:34 或 13:05 拼接当前日期
                    if ("^\\d{2}\\s*:\\s*\\d{2}\\s*:\\s*\\d{2}$".equals(key)
                            || "^\\d{2}\\s*:\\s*\\d{2}$".equals(key)) {
                        dateText = curDate + "-" + dateText;

                        //21.1 (日.月) 拼接当前年份
                    } else if ("^\\d{1,2}\\D+\\d{1,2}$".equals(key)) {
                        dateText = curDate.substring(0, 4) + "-" + dateText;
                    }
                    dateReplace = dateText.replaceAll("\\D+", "-");
                    formatDataStr = formatter.format(timeFormatter.parse(dateReplace));
                    break;
                }
            }
        } catch (Exception e) {
            log.error("cn.fxbin.bubble.core.util.DateUtils.formatDateStr异常", e);
            return "";
        }
        return formatDataStr;
    }


    /**
     * isDateType 判断对象是否为日期类型
     *
     * @since 2020/5/28 11:36
     * @param object object
     * @return boolean
     */
    public boolean isDateType(Object object) {
        boolean isDateType = false;
        if (object instanceof Date) {
            isDateType = true;
        } else if (object instanceof LocalDate) {
            isDateType = true;
        } else if (object instanceof LocalDateTime) {
            isDateType = true;
        }
        return isDateType;
    }


    /**
     * isToday
     *
     * <p>
     *     判断日期是否为今天
     * </p>
     *
     * @since 2021/7/14 15:07
     * @param object 日期对象
     * @return boolean
     */
    public boolean isToday(Object object) {
        LocalDate today = DateUtils.localDate();
        if (object instanceof Date) {
            return today.isEqual(DateUtils.toLocalDate((Date) object));
        } else if (object instanceof LocalDate) {
            return today.isEqual((LocalDate) object);
        } else if (object instanceof LocalDateTime) {
            return today.isEqual(DateUtils.toLocalDate((LocalDateTime) object));
        } else {
            return false;
        }
    }

    // ========= 日期格式化 =========


    /**
     * getCurDateOfNormDatePattern
     *
     * @since 2020/3/23 11:38
     * @return java.lang.String
     */
    public String getCurDateOfNormDatePattern() {
        return getCurDateTime(NORM_DATE_PATTERN);
    }

    /**
     * getCurDateTime
     *
     * @since 2020/4/21 18:16
     * @return java.lang.String
     */
    public String getCurDateTime() {
        return getCurDateTime(NORM_DATETIME_PATTERN);
    }

    /**
     * getCurTime
     *
     * @since 2020/4/21 18:16
     * @return java.lang.String
     */
    public String getCurTime() {
        return getCurDateTime(NORM_TIME_PATTERN);
    }

    /**
     * getCurDateTime
     *
     * @since 2020/3/23 11:37
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String getCurDateTime(@NonNull String pattern) {
        return format(localDateTime(), pattern);
    }

    /**
     * format
     *
     * <p>
     *     根据日期格式 , 将秒转化为指定的 mm:ss 或者 HH:mm:ss 格式
     * </p>
     *
     * @since 2021/7/14 15:10
     * @param totalSeconds total seconds
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(Integer totalSeconds, @NonNull String pattern) {
        if (totalSeconds == null || totalSeconds < 1) {
            return "00:01";
        }

        if ("mm:ss".equals(pattern)) {
            // 将秒格式化成 mm:ss
            // 这里应该用Duration更合理，但它不能格式化成字符串
            // 而使用LocalTime，在时间超过24小时后格式化也会有问题（！）
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } else if ("HH:mm:ss".equals(pattern)) {

            int hours = totalSeconds / 3600;
            int rem = totalSeconds % 3600;
            int minutes = rem / 60;
            int seconds = rem % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "";
        }
    }

    /**
     * format - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 17:55
     * @param date java.util.Date
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(@NonNull Date date, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return toLocalDateTime(date).format(formatter);
    }

    /**
     * format - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 17:54
     * @param localDate java.time.LocalDate
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(@NonNull LocalDate localDate, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return toLocalDateTime(localDate).format(formatter);
    }

    /**
     * format - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 17:54
     * @param localTime java.time.LocalTime
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(@NonNull LocalTime localTime, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return toLocalDateTime(localTime).format(formatter);
    }

    /**
     * format - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/3/23 11:28
     * @param localDateTime java.time.LocalDateTime
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(@NonNull LocalDateTime localDateTime, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return localDateTime.format(formatter);
    }

    /**
     * parseDateOfNormDatePattern
     *
     * @since 2020/4/21 18:14
     * @param dateText date text, not null
     * @return java.util.Date
     */
    public Date parseDateOfNormDatePattern(@NonNull String dateText) {
        return parseDate(dateText, NORM_DATE_PATTERN);
    }

    /**
     * parseLocalDate
     *
     * @since 2020/4/21 18:23
     * @param dateText date text, not null
     * @return java.time.LocalDate
     */
    public LocalDate parseLocalDate(@NonNull String dateText) {
        return parseLocalDate(dateText, NORM_DATE_PATTERN);
    }

    /**
     * parseLocalDateTime
     *
     * @since 2020/4/21 18:14
     * @param dateText date text, not null
     * @return java.time.LocalDateTime
     */
    public LocalDateTime parseLocalDateTime(@NonNull String dateText) {
        return parseLocalDateTime(dateText, NORM_DATETIME_PATTERN);
    }

    /**
     * parseDate - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 18:09
     * @param dateText date text, not null
     * @param pattern the pattern to use, not null
     * @return java.util.Date
     */
    public Date parseDate(@NonNull String dateText, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return parseDate(dateText, formatter);
    }

    /**
     * parseDate
     *
     * @since 2020/4/21 18:08
     * @param dateText date text, not null
     * @param formatter java.time.format.DateTimeFormatter
     * @return java.util.Date
     */
    public Date parseDate(@NonNull String dateText, @NonNull DateTimeFormatter formatter) {
        return toDate(parseLocalDateTime(dateText, formatter));
    }

    /**
     * parseLocalDate - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 18:22
     * @param dateText date text, not null
     * @param pattern the pattern to use, not null
     * @return java.time.LocalDate
     */
    public LocalDate parseLocalDate(@NonNull String dateText, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return parseLocalDate(dateText, formatter);
    }

    /**
     * parseLocalDate
     *
     * @since 2020/4/21 18:22
     * @param dateText date text, not null
     * @param formatter java.time.format.DateTimeFormatter
     * @return java.time.LocalDate
     */
    public LocalDate parseLocalDate(@NonNull String dateText, @NonNull DateTimeFormatter formatter) {
        return LocalDate.parse(dateText, formatter);
    }

    /**
     * parseLocalDateTime - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 18:05
     * @param dateText date text, not null
     * @param pattern the pattern to use, not null
     * @return java.time.LocalDateTime
     */
    public LocalDateTime parseLocalDateTime(@NonNull String dateText, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return LocalDateTime.parse(dateText, formatter);
    }

    /**
     * parseLocalDateTime
     *
     * @since 2020/4/21 18:05
     * @param dateText date text, not null
     * @param formatter java.time.format.DateTimeFormatter
     * @return java.time.LocalDateTime
     */
    public LocalDateTime parseLocalDateTime(@NonNull String dateText, @NonNull DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateText, formatter);
    }

    /**
     * parseZonedDateTime - 优化版本，使用缓存的DateTimeFormatter
     *
     * @since 2020/4/21 18:04
     * @param dateText date text, not null
     * @param pattern the pattern to use, not null
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime parseZonedDateTime(@NonNull String dateText, @NonNull String pattern) {
        DateTimeFormatter formatter = getCachedFormatter(pattern);
        return parseZonedDateTime(dateText, formatter);
    }

    /**
     * parseZonedDateTime
     *
     * @since 2020/4/21 18:03
     * @param dateText date text, not null
     * @param formatter java.time.format.DateTimeFormatter
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime parseZonedDateTime(@NonNull String dateText, @NonNull DateTimeFormatter formatter) {
        return ZonedDateTime.parse(dateText, formatter);
    }


    // ========= 日期-日期 转化 ========= //


    /**
     * toLocalDateTime
     *
     * <p>
     *      parse java.util.Date to java.time.LocalDateTime
     * </p>
     *
     * @since 2020/3/23 11:22
     * @param date java.util.Date
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(@NonNull Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * toLocalDateTime
     *
     * <p>
     *     parse java.time.LocalDate to java.time.LocalDateTime
     * </p>
     *
     * @since 2020/4/21 16:47
     * @param localDate java.time.LocalDate
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(@NonNull LocalDate localDate) {
        return localDate.atStartOfDay();
    }

    /**
     * toLocalDateTime
     *
     * <p>
     *     parse java.time.LocalTime to java.time.LocalDateTime
     * </p>
     *
     * @since 2020/4/21 16:50
     * @param localTime java.time.LocalTime
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(@NonNull LocalTime localTime) {
        return localDate().atTime(localTime);
    }

    /**
     * toLocalDateTime
     *
     * <p>
     *     parse java.time.Instant to java.time.LocalDateTime
     * </p>
     *
     * @since 2020/4/21 16:51
     * @param instant java.time.Instant
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(@NonNull Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * toLocalDateTime
     *
     * <p>
     *     毫秒值 转 java.time.LocalDateTime
     * </p>
     *
     * @since 2020/4/21 16:52
     * @param epochMilli 毫秒值
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    /**
     * toLocalDateTime
     *
     * <p>
     *     java.time.temporal.TemporalAccessor to java.time.LocalDateTime
     * </p>
     *
     * @since 2020/4/21 16:54
     * @param temporalAccessor java.time.temporal.TemporalAccessor
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(@NonNull TemporalAccessor temporalAccessor) {
        return LocalDateTime.from(temporalAccessor);
    }

    /**
     * toLocalDateTime
     *
     * <p>
     *     java.time.ZonedDateTime to java.time.LocalDateTime
     * </p>
     *
     * @since 2020/4/21 16:55
     * @param zonedDateTime java.time.ZonedDateTime
     * @return java.time.LocalDateTime
     */
    public LocalDateTime toLocalDateTime(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * toLocalDate
     *
     * <p>
     *     parse java.util.Date to java.time.LocalDate
     * </p>
     *
     * @since 2020/3/23 11:21
     * @param date java.util.Date
     * @return java.time.LocalDate
     */
    public LocalDate toLocalDate(@NonNull Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * toLocalDate
     *
     * <p>
     *     parse java.time.LocalDateTime to java.time.LocalDate
     * </p>
     *
     * @since 2020/4/21 16:56
     * @param localDateTime java.time.LocalDateTime
     * @return java.time.LocalDate
     */
    public LocalDate toLocalDate(@NonNull LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }

    /**
     * toLocalDate
     *
     * <p>
     *     parse java.time.Instant to java.time.LocalDate
     * </p>
     *
     * @since 2020/4/21 16:58
     * @param instant java.time.Instant
     * @return java.time.LocalDate
     */
    public LocalDate toLocalDate(@NonNull Instant instant) {
        return toLocalDateTime(instant).toLocalDate();
    }

    /**
     * toLocalDate
     *
     * <p>
     *     parse java.time.temporal.TemporalAccessor to java.time.LocalDate
     * </p>
     *
     * @since 2020/4/21 17:00
     * @param temporalAccessor java.time.temporal.TemporalAccessor
     * @return java.time.LocalDate
     */
    public LocalDate toLocalDate(@NonNull TemporalAccessor temporalAccessor) {
        return toLocalDateTime(temporalAccessor).toLocalDate();
    }

    /**
     * toLocalDate
     *
     * <p>
     *     parse java.time.ZonedDateTime to java.time.LocalDate
     * </p>
     *
     * @since 2020/4/21 17:01
     * @param zonedDateTime java.time.ZonedDateTime
     * @return java.time.LocalDate
     */
    public LocalDate toLocalDate(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDate();
    }

    /**
     * toLocalTime
     *
     * <p>
     *     parse java.util.Date to java.time.LocalTime
     * </p>
     *
     * @since 2020/3/23 11:20
     * @param date java.util.Date
     * @return java.time.LocalTime
     */
    public LocalTime toLocalTime(@NonNull Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * toLocalTime
     *
     * <p>
     *     parse java.time.LocalDateTime to java.time.LocalTime
     * </p>
     *
     * @since 2020/4/21 17:03
     * @param localDateTime java.time.LocalDateTime
     * @return java.time.LocalTime
     */
    public LocalTime toLocalTime(@NonNull LocalDateTime localDateTime) {
        return localDateTime.toLocalTime();
    }

    /**
     * toLocalTime
     *
     * <p>
     *     parse java.time.Instant to java.time.LocalTime
     * </p>
     *
     * @since 2020/4/21 17:05
     * @param instant java.time.Instant
     * @return java.time.LocalTime
     */
    public LocalTime toLocalTime(@NonNull Instant instant) {
        return toLocalDateTime(instant).toLocalTime();
    }

    /**
     * toLocalTime
     *
     * <p>
     *     parse java.time.temporal.TemporalAccessor to java.time.LocalTime
     * </p>
     *
     * @since 2020/4/21 17:05
     * @param temporalAccessor java.time.temporal.TemporalAccessor
     * @return java.time.LocalTime
     */
    public LocalTime toLocalTime(@NonNull TemporalAccessor temporalAccessor) {
        return LocalTime.from(temporalAccessor);
    }

    /**
     * toLocalTime
     *
     * <p>
     *     parse java.time.ZonedDateTime to java.time.LocalTime
     * </p>
     *
     * @since 2020/4/21 17:06
     * @param zonedDateTime java.time.ZonedDateTime
     * @return java.time.LocalTime
     */
    public LocalTime toLocalTime(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalTime();
    }

    /**
     * toInstant
     *
     * <p>
     *    parse java.util.Date to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:07
     * @param date java.util.Date
     * @return java.time.Instant
     */
    public Instant toInstant(@NonNull Date date) {
        return date.toInstant();
    }

    /**
     * toInstant
     *
     * <p>
     *     parse java.time.LocalDateTime to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:07
     * @param localDateTime java.time.LocalDateTime
     * @return java.time.Instant
     */
    public Instant toInstant(@NonNull LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * toInstant
     *
     * <p>
     *     parse java.time.LocalDate to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:08
     * @param localDate java.time.LocalDate
     * @return java.time.Instant
     */
    public Instant toInstant(@NonNull LocalDate localDate) {
        return toLocalDateTime(localDate).atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * toInstant
     *
     * <p>
     *     parse java.time.LocalTime to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:10
     * @param localTime java.time.LocalTime
     * @return java.time.Instant
     */
    public Instant toInstant(@NonNull LocalTime localTime) {
        return toLocalDateTime(localTime).atZone(ZoneId.systemDefault()).toInstant();
    }


    /**
     * toInstant
     *
     * <p>
     *     parse 毫秒值 to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:10
     * @param epochMilli 毫秒值
     * @return java.time.Instant
     */
    public Instant toInstant(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }


    /**
     * toInstant
     *
     * <p>
     *    parse java.time.temporal.TemporalAccessor to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:12
     * @param temporalAccessor java.time.temporal.TemporalAccessor
     * @return java.time.Instant
     */
    public Instant toInstant(@NonNull TemporalAccessor temporalAccessor) {
        return Instant.from(temporalAccessor);
    }

    /**
     * toInstant
     *
     * <p>
     *     parse java.time.ZonedDateTime to java.time.Instant
     * </p>
     *
     * @since 2020/4/21 17:12
     * @param zonedDateTime java.time.ZonedDateTime
     * @return java.time.Instant
     */
    public Instant toInstant(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant();
    }

    public long toEpochMilli(@NonNull Object object){
        if (object instanceof Date) {
            return toEpochMilli((Date) object);
        } else if (object instanceof LocalDate) {
            return toEpochMilli((LocalDate) object);
        } else if (object instanceof LocalDateTime) {
            return toEpochMilli((LocalDateTime) object);
        } else {
            throw new UtilException("入参不属于日期类型");
        }
    }

    /**
     * toEpochMilli(从1970-01-01T00:00:00Z开始的毫秒值)
     *
     * @since 2020/4/21 17:13
     * @param date java.util.Date
     * @return long 毫秒值
     */
    public long toEpochMilli(@NonNull Date date){
        return date.getTime();
    }

    /**
     * toEpochMilli(从1970-01-01T00:00:00Z开始的毫秒值)
     *
     * @since 2020/4/21 17:18
     * @param localDateTime java.time.LocalDateTime
     * @return long 毫秒值
     */
    public long toEpochMilli(@NonNull LocalDateTime localDateTime){
        return toInstant(localDateTime).toEpochMilli();
    }

    /**
     * toEpochMilli(从1970-01-01T00:00:00Z开始的毫秒值)
     *
     * @since 2020/4/21 17:19
     * @param localDate java.time.LocalDate
     * @return long 毫秒值
     */
    public long toEpochMilli(@NonNull LocalDate localDate){
        return toInstant(localDate).toEpochMilli();
    }

    /**
     * toEpochMilli(从1970-01-01T00:00:00Z开始的毫秒值)
     *
     * @since 2020/4/21 17:20
     * @param instant java.time.Instant
     * @return long 毫秒值
     */
    public long toEpochMilli(@NonNull Instant instant){
        return instant.toEpochMilli();
    }

    /**
     * toEpochMilli(从1970-01-01T00:00:00Z开始的毫秒值)
     * 注： java.time.ZonedDateTime 时区需与系统时区一致
     *
     * @since 2020/4/21 17:20
     * @param zonedDateTime java.time.ZonedDateTime
     * @return long 毫秒值
     */
    public long toEpochMilli(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * toZonedDateTime
     *
     * <p>
     *     parse java.util.Date to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:25
     * @param date java.util.Date
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(@NonNull Date date) {
        return toLocalDateTime(date).atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime
     *
     * <p>
     *     parse java.time.LocalDateTime to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:25
     * @param localDateTime java.time.LocalDateTime
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(@NonNull LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime
     *
     * <p>
     *     parse java.time.LocalDateTime(with time-zone ID) to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:27
     * @param localDateTime java.time.LocalDateTime
     * @param zoneId the time-zone ID, not null
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(@NonNull LocalDateTime localDateTime, @NonNull String zoneId) {
        return localDateTime.atZone(ZoneId.of(zoneId));
    }


    /**
     * toZonedDateTime
     *
     * <p>
     *     parse java.time.LocalDate to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:27
     * @param localDate java.time.LocalDate
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(@NonNull LocalDate localDate) {
        return toLocalDateTime(localDate).atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime
     *
     * <p>
     *     parse java.time.LocalTime to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:31
     * @param localTime java.time.LocalTime
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(@NonNull LocalTime localTime) {
        return toLocalDateTime(localTime).atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime
     *
     * <p>
     *     parse java.time.temporal.TemporalAccessor to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:47
     * @param temporalAccessor java.time.temporal.TemporalAccessor
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(@NonNull TemporalAccessor temporalAccessor) {
        return toLocalDateTime(temporalAccessor).atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime
     *
     * <p>
     *     parse 毫秒值 to java.time.ZonedDateTime
     * </p>
     *
     * @since 2020/4/21 17:48
     * @param epochMilli 毫秒值
     * @return java.time.ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(long epochMilli) {
        return toLocalDateTime(epochMilli).atZone(ZoneId.systemDefault());
    }

    /**
     * toDate
     *
     * <p>
     *     parse java.time.ZonedDateTime to java.util.Date
     * </p>
     *
     * @since 2020/4/9 19:11
     * @param zonedDateTime java.time.ZonedDateTime
     * @return java.util.Date
     */
    public Date toDate(@NonNull ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * toDate
     *
     * <p>
     *     parse epoch milli to java.util.Date
     * </p>
     *
     * @since 2020/4/9 19:10
     * @param epochMilli epoch milli
     * @return java.util.Date
     */
    public Date toDate(long epochMilli){
        return new Date(epochMilli);
    }

    /**
     * toDate
     *
     * <p>
     *     parse java.time.Instant to java.util.Date
     * </p>
     *
     * @since 2020/4/9 19:09
     * @param instant java.time.Instant
     * @return java.util.Date
     */
    public Date toDate(@NonNull Instant instant) {
        return Date.from(instant);
    }

    /**
     * toDate
     *
     * <p>
     *     parse java.time.LocalTime to java.util.Date
     * </p>
     *
     * @since 2020/4/9 19:08
     * @param localTime java.time.LocalTime
     * @return java.util.Date
     */
    public Date toDate(@NonNull LocalTime localTime) {
        return Date.from(LocalDate.now().atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * toDate
     *
     * <p>
     *     parse java.time.LocalDate to java.util.Date
     * </p>
     *
     * @since 2020/3/23 11:19
     * @param localDate java.time.LocalDate
     * @return java.util.Date
     */
    public Date toDate(@NonNull LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * toDate
     *
     * <p>
     *  parse java.time.LocalDateTime to java.util.Date
     * </p>
     *
     * @since 2020/3/23 11:19
     * @param localDateTime java.time.LocalDateTime
     * @return java.util.Date
     */
    public Date toDate(@NonNull LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    // ========= 获取日期 =========


    /**
     * firstDayOfMonth
     *
     * @since 2020/3/23 11:26
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfMonth(Date date) {
        return toLocalDate(date).with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * firstDayOfNextMonth
     *
     * @since 2020/3/23 11:25
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfNextMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
    }

    /**
     * firstDayOfMonth
     *
     * @since 2020/3/23 11:25
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * lastDayOfMonth
     *
     * @since 2020/3/23 11:25
     * @return java.time.LocalDate
     */
    public LocalDate lastDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * firstDayOfNextYear
     *
     * @since 2020/3/23 11:24
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfNextYear() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextYear());
    }

    /**
     * firstDayOfYear
     *
     * @since 2020/3/23 11:24
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfYear() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * lastDayOfYear
     *
     * @since 2020/3/23 11:24
     * @return java.time.LocalDate
     */
    public LocalDate lastDayOfYear() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * isLeapYear
     *
     * <p>
     *     当前时间是否为闰年
     * </p>
     *
     * @since 2020/3/23 11:22
     * @return boolean
     */
    public boolean isLeapYear() {
        return YearMonth.from(LocalDate.now()).isLeapYear();
    }


    // ========= 日期获取 ========= //


    /**
     * localTime
     *
     * @since 2020/3/23 11:18
     * @return java.time.LocalTime
     */
    public LocalTime localTime() {
        return LocalTime.now();
    }

    /**
     * localDate
     *
     * @since 2020/3/23 11:18
     * @return java.time.LocalDate
     */
    public LocalDate localDate() {
        return LocalDate.now();
    }

    /**
     * localDateTime
     *
     * @since 2020/3/23 11:18
     * @return java.time.LocalDateTime
     */
    public LocalDateTime localDateTime() {
        return LocalDateTime.now();
    }

}
