package cn.fxbin.bubble.fireworks.core.util.time;

/**
 * Constant
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/21 16:34
 */
public interface TimePool {

    /**
     * 每天小时数
     */
    Integer HOURS_PER_DAY = 24;

    /**
     * 每小时分钟数
     */
    Integer MINUTES_PER_HOUR = 60;

    /**
     * 每分钟秒数
     */
    Integer SECONDS_PER_MINUTE = 60;

    /**
     * 每秒毫秒数
     */
    Integer MILLISECOND_PER_SECONDS = 1000;

    /**
     * 每天秒数
     */
    Integer SECONDS_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;

    /**
     * 每天毫秒数
     */
    Integer MILLISECOND_PER_DAY = SECONDS_PER_DAY * MILLISECOND_PER_SECONDS;


}
