package cn.fxbin.bubble.fireworks.plugin.logging.report;

import cn.fxbin.bubble.fireworks.plugin.logging.constant.BeanKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * LoggingReportScheduled
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 14:35
 */
@Slf4j
@Component
@ConditionalOnBean(name = {BeanKey.ALIYUN_LOGGING_REPORT})
public class LoggingReportScheduled {

    @Resource
    private LoggingReport loggingReport;

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public LoggingReportScheduled() {
        executorService.scheduleAtFixedRate(() -> {

            try {
                loggingReport.report();
            } catch (Exception e) {
                log.error("日志延时批量上报失败", e);
            }

        }, 2, 10, TimeUnit.SECONDS);
    }

}
