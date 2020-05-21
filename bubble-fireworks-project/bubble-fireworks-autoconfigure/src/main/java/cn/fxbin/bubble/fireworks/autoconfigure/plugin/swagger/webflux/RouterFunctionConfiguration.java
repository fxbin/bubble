package cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.webflux;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.SwaggerProperties.BUBBLE_FIREWORKS_SWAGGER_PREFIX;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

/**
 * RouterFunctionConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/3 10:54
 */
@Configuration(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackages = {"cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.webflux"}
)
@ConditionalOnClass({Docket.class, RouterFunction.class})
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = BUBBLE_FIREWORKS_SWAGGER_PREFIX, name = "enabled", havingValue = "true")
public class RouterFunctionConfiguration {

    @Resource
    private SwaggerResourceHandler swaggerResourceHandler;

    @Resource
    private SwaggerSecurityHandler swaggerSecurityHandler;

    @Resource
    private SwaggerUiHandler swaggerUiHandler;

    @Bean
    public RouterFunction<?> routerFunction() {
        return RouterFunctions.route(
                GET("/swagger-resources").and(accept(MediaType.ALL)), swaggerResourceHandler)
                .andRoute(GET("/swagger-resources/configuration/ui")
                        .and(accept(MediaType.ALL)), swaggerUiHandler)
                .andRoute(GET("/swagger-resources/configuration/security")
                        .and(accept(MediaType.ALL)), swaggerSecurityHandler);
    }

}
