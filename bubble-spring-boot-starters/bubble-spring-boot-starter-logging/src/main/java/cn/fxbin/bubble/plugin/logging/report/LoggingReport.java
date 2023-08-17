package cn.fxbin.bubble.plugin.logging.report;

import cn.fxbin.bubble.plugin.logging.model.BubbleFireworksLogging;

import java.util.List;

/**
 * LoggingReport
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 14:34
 */
public interface LoggingReport {

    /**
     * report
     *
     * @since 2020/5/19 14:34
     */
    void report();

    /**
     * report
     *
     * @since 2020/5/19 14:34
     * @param logs report logs
     */
    void report(List<BubbleFireworksLogging> logs);

}
