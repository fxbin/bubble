package cn.fxbin.bubble.fireworks.autofigure.plugin.swagger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import springfox.documentation.spring.web.plugins.Docket;

import static cn.fxbin.bubble.fireworks.autofigure.plugin.swagger.SwaggerProperties.BUBBLE_FIREWORKS_SWAGGER_PREFIX;

/**
 * SwaggerWebFluxAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/31 18:54
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass(Docket.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(prefix = BUBBLE_FIREWORKS_SWAGGER_PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingClass("org.springframework.cloud.gateway.config.GatewayAutoConfiguration")
public class SwaggerWebFluxAutoConfiguration implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars*")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
