package cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.webflux;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.SwaggerProperties.BUBBLE_FIREWORKS_SWAGGER_PREFIX;

/**
 * SwaggerWebFluxAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 11:50
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(prefix = BUBBLE_FIREWORKS_SWAGGER_PREFIX, name = "enabled", havingValue = "true")
public class SwaggerWebFluxAutoConfiguration implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

}
