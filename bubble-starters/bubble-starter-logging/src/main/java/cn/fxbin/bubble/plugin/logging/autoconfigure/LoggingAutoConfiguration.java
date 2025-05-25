package cn.fxbin.bubble.plugin.logging.autoconfigure;

import cn.fxbin.bubble.plugin.logging.aspect.LoggingWebAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * LoggingAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 15:56
 */
@Configuration(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackages = {"cn.fxbin.bubble.plugin.logging"}
)
@ConditionalOnClass(LoggingWebAspect.class)
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = LoggingProperties.BUBBLE_LOGGING_PREFIX, name = "enabled", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoggingWebAspect loggingWebAspect() {
        return new LoggingWebAspect();
    }

}
