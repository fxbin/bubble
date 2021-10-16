package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.autoconfigure;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.autoconfigure.listener.ConfigServerHandler;
import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.handler.ConfigListenerHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ConfigServerRefreshAutoConfigure
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/14 1:26
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({ConfigServicePropertySourceLocator.class, ConfigListenerHandler.class})
public class ConfigServerRefreshAutoConfiguration {

    @Bean
    public ConfigServerHandler configServerHandler() {
        return new ConfigServerHandler();
    }

}
