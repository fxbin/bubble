package cn.fxbin.bubble.fireworks.plugin.logging.report;

import cn.fxbin.bubble.fireworks.plugin.logging.exception.LoggingException;
import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import cn.fxbin.bubble.fireworks.plugin.logging.cache.LoggingCache;
import cn.fxbin.bubble.fireworks.plugin.logging.constant.ReportType;
import cn.fxbin.bubble.fireworks.plugin.logging.model.BubbleFireworksLogging;
import cn.fxbin.bubble.fireworks.plugin.logging.util.LoggingUtils;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.aliyun.log.producer.errors.LogSizeTooLargeException;
import com.aliyun.openservices.aliyun.log.producer.errors.MaxBatchCountExceedException;
import com.aliyun.openservices.aliyun.log.producer.errors.ResultFailedException;
import com.aliyun.openservices.aliyun.log.producer.errors.TimeoutException;
import com.aliyun.openservices.log.common.LogItem;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LoggingReportSupport
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 14:35
 */
@Slf4j
public class AliyunLoggingReport implements LoggingReport {

    private static final ExecutorService EXECUTOR_SERVICE = Executors
            .newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 1));


    @Getter
    @Setter
    private String projectName;

    @Getter
    @Setter
    private String logStore;

    @Getter
    @Setter
    private Integer numberOfReportLog;

    @Getter
    @Setter
    private ReportType reportType;

    @Resource
    private LoggingCache loggingCache;

    private final Producer producer;

    public AliyunLoggingReport(Producer producer) {
        this.producer = producer;
    }

    @Override
    public void report() {
        Integer numberOfReportLog = getNumberOfReportLog();
        List<BubbleFireworksLogging> logs = new ArrayList<>();
        try {
            logs = loggingCache.getLogs(numberOfReportLog);
            report(logs);
        } catch (LoggingException e) {
            log.error("日志上报失败", e);
            if (ObjectUtils.isNotEmpty(logs)) {
                logs.forEach(log -> loggingCache.cache(log));
            }
        }
    }

    @Override
    public void report(List<BubbleFireworksLogging> logs) {
        if (ObjectUtils.isEmpty(logs)) {
            log.debug("Don't have logs , report logs over");
            return;
        }

        log.debug("log report start...");

        for (BubbleFireworksLogging fireworksLogging : logs) {
            LogItem logItem = LoggingUtils.generateLogItem(fireworksLogging);
            try {
                ListenableFuture<Result> listenableFuture = producer.send(
                        getProjectName(),
                        getLogStore(),
                        logItem
                );

                Futures.addCallback(
                        listenableFuture,
                        new SampleFutureCallback(
                                getProjectName(), getLogStore(),
                                Lists.newArrayList(logItem)),
                        EXECUTOR_SERVICE
                );

            } catch (InterruptedException e) {
                log.warn("The current thread has been interrupted during send logs.", e);
            } catch (MaxBatchCountExceedException e){
                log.error("The logs exceeds the maximum batch count", e);
            } catch (LogSizeTooLargeException e) {
                log.error("The size of log is larger than the maximum allowable size", e);
            } catch (TimeoutException e) {
                log.error("The time taken for allocating memory for the logs has surpassed.", e);
            } catch (Exception e) {
                log.error("Failed to send logs", e);
            }
        }
    }

    private static final class SampleFutureCallback implements FutureCallback<Result> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SampleFutureCallback.class);

        private final String project;

        private final String logStore;

        private final List<LogItem> logItems;

        SampleFutureCallback(
                String project, String logStore, List<LogItem> logItems) {
            this.project = project;
            this.logStore = logStore;
            this.logItems = logItems;
        }

        @Override
        public void onSuccess(@Nullable Result result) {
            LOGGER.info("Send logs successfully.");
        }

        @Override
        public void onFailure(Throwable throwable) {
            if (throwable instanceof ResultFailedException) {
                Result result = ((ResultFailedException) throwable).getResult();
                LOGGER.error(
                        "Failed to send logs, project={}, logStore={}, result={}", project, logStore, result);
            } else {
                LOGGER.error("Failed to send log, e=", throwable);
            }
        }
    }


}
