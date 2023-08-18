package cn.fxbin.bubble.plugin.logging.autoconfigure;

import cn.fxbin.bubble.plugin.logging.constant.ReportType;
import cn.fxbin.bubble.plugin.logging.constant.RetryStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * LoggingProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 15:34
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = LoggingProperties.BUBBLE_FIREWORKS_LOGGING_PREFIX)
public class LoggingProperties {

    public static final String BUBBLE_FIREWORKS_LOGGING_PREFIX = "bubble.logging";

    public static final String BUBBLE_FIREWORKS_LOGGING_ALIYUN_PREFIX = "bubble.logging.aliyun";

    /**
     * 默认开启日志
     */
    private boolean enabled = true;

    /**
     * 日志上报方式, 默认实时
     */
    private ReportType reportType = ReportType.realtime;

    /**
     * 定时上报的日志数量，默认10条
     */
    private Integer numberOfReportLog = 10;

    /**
     * 上报失败重试策略(todo), 默认丢弃
     */
    private RetryStrategy retryStrategy = RetryStrategy.discard;

    /**
     * 忽略的uri
     */
    private List<String> ignoreUris;

    /**
     * 阿里云日志服务配置项
     */
    private AliyunLogConfig aliyun = new AliyunLogConfig();
    
    @Data
    public static class AliyunLogConfig {

        /**
         * 日志项目名
         */
        private String projectName;
        
        /**
         * 日志库
         */
        private String logStore;
        
        /**
         * 阿里云 Region 服务入口
         */
        private String endpoint;

        /**
         * accessKeyId
         */
        private String accessKeyId;

        /**
         * accessKeySecret
         */
        private String accessKeySecret;
    }
}
