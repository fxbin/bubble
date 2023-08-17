package cn.fxbin.bubble.plugin.logging.event.runner;

import cn.fxbin.bubble.plugin.logging.cache.LoggingCache;
import cn.fxbin.bubble.plugin.logging.constant.BeanKey;
import cn.fxbin.bubble.plugin.logging.constant.ReportType;
import cn.fxbin.bubble.plugin.logging.event.LoggingNoticeEvent;
import cn.fxbin.bubble.plugin.logging.model.BubbleFireworksLogging;
import cn.fxbin.bubble.plugin.logging.report.AliyunLoggingReport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * LoggingNoticeRunner
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 14:33
 */
@Slf4j
@Component
@ConditionalOnBean(name = {BeanKey.MEMORY_LOGGING_CACHE, BeanKey.ALIYUN_LOGGING_REPORT})
public class LoggingNoticeRunner {

    @Resource
    private AliyunLoggingReport aliyunLoggingReport;

    @Resource(name = BeanKey.MEMORY_LOGGING_CACHE)
    private LoggingCache loggingCache;

    @SneakyThrows
    @ConditionalOnBean(name = BeanKey.ALIYUN_LOGGING_REPORT)
    @EventListener({LoggingNoticeEvent.class})
    public void run(Object object) {
        LoggingNoticeEvent event = (LoggingNoticeEvent) object;
        BubbleFireworksLogging fireworksLogging = event.getFireworksLogging();

        ReportType reportType = aliyunLoggingReport.getReportType();
        switch (reportType) {
            case delay:
                loggingCache.cache(fireworksLogging);
                log.debug("Log cache complete.");
                break;
            case realtime:
                aliyunLoggingReport.report(Collections.singletonList(fireworksLogging));
                break;
            default:
                break;
        }

    }

}
