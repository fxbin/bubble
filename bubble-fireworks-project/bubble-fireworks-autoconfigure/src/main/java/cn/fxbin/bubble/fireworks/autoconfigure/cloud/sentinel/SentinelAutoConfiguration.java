package cn.fxbin.bubble.fireworks.autoconfigure.cloud.sentinel;

import cn.fxbin.bubble.fireworks.cloud.sentinel.handler.CustomUrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SentinelAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/31 16:58
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(SentinelWebInterceptor.class)
public class SentinelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BlockExceptionHandler blockExceptionHandler() {
        return new CustomUrlBlockHandler();
    }

}
