package cn.fxbin.bubble.fireworks.autoconfigure.plugin.logging;

import cn.fxbin.bubble.fireworks.plugin.logging.aspect.LoggingWebAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.logging.LoggingProperties.BUBBLE_FIREWORKS_LOGGING_PREFIX;

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
        basePackages = {"cn.fxbin.bubble.fireworks.plugin.logging"}
)
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = BUBBLE_FIREWORKS_LOGGING_PREFIX, name = "enabled", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoggingWebAspect loggingWebAspect() {
        return new LoggingWebAspect();
    }

}
