package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DateUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:09
 */
@Slf4j
@UtilityClass
public class DateUtils {

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
    public static final String NORM_I18N_DATETIME_FORMAT = "dd/MM/yyy HH:mm:ss";

    /** 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS */
    public final String NORM_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

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
    public static String formatDateStr(String dateStr) {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN);

        String dateReplace;
        DateTimeFormatter timeFormatter;
        String formatDataStr = "";
        try {
            for (String key : dateRegExpress.keySet()) {
                if (Pattern.compile(key).matcher(dateStr).matches()) {

                    timeFormatter = DateTimeFormatter.ofPattern(dateRegExpress.get(key));
                    //13:05:34 或 13:05 拼接当前日期
                    if ("^\\d{2}\\s*:\\s*\\d{2}\\s*:\\s*\\d{2}$".equals(key)
                            || "^\\d{2}\\s*:\\s*\\d{2}$".equals(key)) {
                        dateStr = curDate + "-" + dateStr;

                        //21.1 (日.月) 拼接当前年份
                    } else if ("^\\d{1,2}\\D+\\d{1,2}$".equals(key)) {
                        dateStr = curDate.substring(0, 4) + "-" + dateStr;
                    }
                    dateReplace = dateStr.replaceAll("\\D+", "-");
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
     * getCurDateOfNormDatePattern
     *
     * @author fxbin
     * @since 2020/3/23 11:38
     * @return java.lang.String
     */
    public String getCurDateOfNormDatePattern() {
        return getCurDateTime(NORM_DATE_PATTERN);
    }

    /**
     * getCurDateTime
     *
     * @author fxbin
     * @since 2020/3/23 11:37
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String getCurDateTime(@NonNull String pattern) {
        return format(localDateTime(), pattern);
    }

    /**
     * parse
     *
     * @author fxbin
     * @since 2020/3/23 11:31
     * @param dateString date string
     * @param pattern the pattern to use, not null
     * @return java.time.LocalDateTime
     */
    public LocalDateTime parse(String dateString, @NonNull String pattern) {
        return parse(dateString, pattern, ZoneId.systemDefault());
    }

    /**
     * parse
     *
     * @author fxbin
     * @since 2020/3/23 11:30
     * @param dateString date string
     * @param pattern the pattern to use, not null
     * @param zoneId the time-zone ID, not null
     * @return java.time.LocalDateTime
     */
    public LocalDateTime parse(String dateString, @NonNull String pattern, @NonNull ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
        return LocalDateTime.parse(dateString, formatter);
    }

    /**
     * format
     *
     * @author fxbin
     * @since 2020/3/23 11:28
     * @param localDateTime java.time.LocalDateTime
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(LocalDateTime localDateTime, @NonNull String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }

    /**
     * format
     *
     * @author fxbin
     * @since 2020/3/23 11:26
     * @param localDate java.time.LocalDate
     * @param pattern the pattern to use, not null
     * @return java.lang.String
     */
    public String format(LocalDate localDate, @NonNull String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDate.format(formatter);
    }

    /**
     * firstDayOfMonth
     *
     * @author fxbin
     * @since 2020/3/23 11:26
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfMonth(Date date) {
        return parseDate2Ld(date).with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * firstDayOfNextMonth
     *
     * @author fxbin
     * @since 2020/3/23 11:25
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfNextMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
    }

    /**
     * firstDayOfMonth
     *
     * @author fxbin
     * @since 2020/3/23 11:25
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * lastDayOfMonth
     *
     * @author fxbin
     * @since 2020/3/23 11:25
     * @return java.time.LocalDate
     */
    public LocalDate lastDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * firstDayOfNextYear
     *
     * @author fxbin
     * @since 2020/3/23 11:24
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfNextYear() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextYear());
    }

    /**
     * firstDayOfYear
     *
     * @author fxbin
     * @since 2020/3/23 11:24
     * @return java.time.LocalDate
     */
    public LocalDate firstDayOfYear() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * lastDayOfYear
     *
     * @author fxbin
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
     * @author fxbin
     * @since 2020/3/23 11:22
     * @return boolean
     */
    public boolean isLeapYear() {
        return YearMonth.from(LocalDate.now()).isLeapYear();
    }

    /**
     * parseDate2Ldt
     *
     * <p>
     *      parse java.util.Date to java.time.LocalDateTime
     * </p>
     *
     * @author fxbin
     * @since 2020/3/23 11:22
     * @param date java.util.Date
     * @return java.time.LocalDateTime
     */
    public LocalDateTime parseDate2Ldt(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * parseDate2Ld
     *
     * <p>
     *     parse java.util.Date to java.time.LocalDate
     * </p>
     *
     * @author fxbin
     * @since 2020/3/23 11:21
     * @param date java.util.Date
     * @return java.time.LocalDate
     */
    public LocalDate parseDate2Ld(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * parseDate2Lt
     *
     * <p>
     *     parse java.util.Date to java.time.LocalTime
     * </p>
     *
     * @author fxbin
     * @since 2020/3/23 11:20
     * @param date java.util.Date
     * @return java.time.LocalTime
     */
    public LocalTime parseDate2Lt(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * parseLd2Date
     *
     * <p>
     *     parse java.time.LocalDate to java.util.Date
     * </p>
     *
     * @author fxbin
     * @since 2020/3/23 11:19
     * @param localDate java.time.LocalDate
     * @return java.util.Date
     */
    public Date parseLd2Date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * parseLdt2Date
     *
     * <p>
     *  parse java.time.LocalDateTime to java.util.Date
     * </p>
     *
     * @author fxbin
     * @since 2020/3/23 11:19
     * @param localDateTime java.time.LocalDateTime
     * @return java.util.Date
     */
    public Date parseLdt2Date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * localTime
     *
     * @author fxbin
     * @since 2020/3/23 11:18
     * @return java.time.LocalTime
     */
    public LocalTime localTime() {
        return LocalTime.now();
    }

    /**
     * localDate
     *
     * @author fxbin
     * @since 2020/3/23 11:18
     * @return java.time.LocalDate
     */
    public LocalDate localDate() {
        return LocalDate.now();
    }

    /**
     * localDateTime
     *
     * @author fxbin
     * @since 2020/3/23 11:18
     * @return java.time.LocalDateTime
     */
    public LocalDateTime localDateTime() {
        return LocalDateTime.now();
    }

}
