package cn.fxbin.bubble.plugin.logging.autoconfigure;

import cn.fxbin.bubble.plugin.logging.cache.LoggingCache;
import cn.fxbin.bubble.plugin.logging.cache.MemoryLoggingCache;
import cn.fxbin.bubble.plugin.logging.constant.BeanKey;
import cn.fxbin.bubble.plugin.logging.report.AliyunLoggingReport;
import cn.fxbin.bubble.plugin.logging.report.LoggingReport;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * AliyunLogContext
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/20 14:33
 */
@Configuration(
        proxyBeanMethods = false
)
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = LoggingProperties.BUBBLE_FIREWORKS_LOGGING_ALIYUN_PREFIX, name = "project-name", matchIfMissing = false)
public class AliyunLogContextAutoConfigure {

    @Resource
    private LoggingProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public ProjectConfig projectConfig() {
        Assert.notNull(properties.getAliyun().getProjectName(), "Project Name Must defined");
        Assert.notNull(properties.getAliyun().getEndpoint(), "Aliyun Endpoint Must defined");
        Assert.notNull(properties.getAliyun().getAccessKeyId(), "AccessKeyId Must defined");
        Assert.notNull(properties.getAliyun().getAccessKeySecret(), "AccessKeySecret Must defined");

        return new ProjectConfig(properties.getAliyun().getProjectName(),
                properties.getAliyun().getEndpoint(),
                properties.getAliyun().getAccessKeyId(),
                properties.getAliyun().getAccessKeySecret());
    }

    @Bean
    @ConditionalOnMissingBean
    public Producer producer(@Qualifier("projectConfig") ProjectConfig projectConfig) {
        Producer producer =  new LogProducer(new ProducerConfig());
        producer.putProjectConfig(projectConfig);
        return producer;
    }

    @Bean(name = BeanKey.ALIYUN_LOGGING_REPORT)
    @ConditionalOnMissingBean(name = {BeanKey.ALIYUN_LOGGING_REPORT})
    public LoggingReport aliyunLoggingReport(@Qualifier("producer") Producer producer) {
        AliyunLoggingReport aliyunLoggingReport = new AliyunLoggingReport(producer);
        aliyunLoggingReport.setProjectName(properties.getAliyun().getProjectName());
        aliyunLoggingReport.setLogStore(properties.getAliyun().getLogStore());
        aliyunLoggingReport.setNumberOfReportLog(properties.getNumberOfReportLog());
        aliyunLoggingReport.setReportType(properties.getReportType());
        return aliyunLoggingReport;
    }

    @Bean(name = BeanKey.MEMORY_LOGGING_CACHE)
    @ConditionalOnMissingBean
    public LoggingCache loggingCache() {
        return new MemoryLoggingCache();
    }

}
