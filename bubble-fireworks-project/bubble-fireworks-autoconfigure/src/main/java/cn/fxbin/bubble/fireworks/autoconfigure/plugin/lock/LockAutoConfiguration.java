package cn.fxbin.bubble.fireworks.autoconfigure.plugin.lock;

import cn.fxbin.bubble.fireworks.plugin.lock.aop.support.LockActionAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * LockAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 14:05
 */
@Configuration(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackages = "cn.fxbin.bubble.fireworks.plugin.lock"
)
@ConditionalOnClass({LockActionAspect.class})
public class LockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LockActionAspect lockInterceptor() {
        return new LockActionAspect();
    }

}
