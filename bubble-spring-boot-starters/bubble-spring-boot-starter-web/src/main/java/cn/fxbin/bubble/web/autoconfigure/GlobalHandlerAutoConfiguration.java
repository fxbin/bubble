package cn.fxbin.bubble.web.autoconfigure;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * GlobalHandlerAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/28 14:06
 */
@Configuration(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackages = {"cn.fxbin.bubble.web.handler"}
)
public class GlobalHandlerAutoConfiguration {
}
