package cn.fxbin.bubble.fireworks.plugin.logging.constant;

/**
 * RetryStrategy
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 13:48
 */
public enum RetryStrategy {

    /**
     * 失败丢弃
     */
    discard,

    /**
     * 失败重试一次
     */
    retry_one;

}
