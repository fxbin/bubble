package cn.fxbin.bubble.plugin.logging.cache;

import cn.fxbin.bubble.plugin.logging.model.BubbleFireworksLogging;

import java.util.List;

/**
 * LoggingCache
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 13:49
 */
public interface LoggingCache {

    /**
     * cache
     *
     * @since 2020/5/19 13:52
     * @param fireworksLogging cn.fxbin.bubble.logging.model.BubbleFireworksLogging
     */
    void cache(BubbleFireworksLogging fireworksLogging);

    /**
     * getLogs
     *
     * @since 2020/5/19 13:54
     * @param count 日志数量
     * @return java.util.List<cn.fxbin.bubble.logging.model.BubbleFireworksLogging>
     */
    List<BubbleFireworksLogging> getLogs(Integer count);

    /**
     * getAllLogs
     *
     * @since 2020/5/19 13:53
     * @return java.util.List<cn.fxbin.bubble.logging.model.BubbleFireworksLogging>
     */
    List<BubbleFireworksLogging> getAllLogs();

}
