package cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.SwaggerProperties.BUBBLE_FIREWORKS_SWAGGER_PREFIX;

/**
 * SwaggerAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/31 18:00
 */
@EnableKnife4j
@EnableSwagger2
@Configuration(
        proxyBeanMethods = false
)
@AutoConfigureBefore(Knife4jAutoConfiguration.class)
@ConditionalOnClass(Docket.class)
@Import(BeanValidatorPluginsConfiguration.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnMissingClass({"org.springframework.cloud.gateway.config.GatewayAutoConfiguration"})
@ConditionalOnProperty(prefix = BUBBLE_FIREWORKS_SWAGGER_PREFIX, name = "enabled", havingValue = "true")
public class SwaggerAutoConfiguration {

    /**
     * 默认解析全部url规则
     */
    private static final String BASE_PATH = "/**";

    /**
     * The name of the SpringBoot Application Name
     */
    public static final String APPLICATION_NAME = "spring.application.name";

    /**
     * 默认的基于BASE_PATH 需要排除的规则, 排除SpringBoot默认的错误处理路径和端点
     */
    private static final List<String> DEFAULT_EXCLUDE_PATH = Arrays.asList("/**/error/**", "/**/actuator/**");

    @Resource
    private Environment environment;

    @Resource
    private SwaggerProperties properties;

    /**
     * docket  创建Docket对象
     *
     * @since 2020/3/31 18:47
     * @return springfox.documentation.spring.web.plugins.Docket
     */
    @Bean
    public Docket docket() {

        // base path 处理
        if(properties.getBasePath().isEmpty()){
            properties.getBasePath().add(BASE_PATH);
        }

        // exclude path 处理
        if(properties.getExcludeBasePath().isEmpty()){
            properties.getExcludeBasePath().addAll(DEFAULT_EXCLUDE_PATH);
        }

        ApiSelectorBuilder builder = new Docket(DocumentationType.SWAGGER_2)
                .host(properties.getHost())
                .apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage(properties.getBasePackage()));

        properties.getBasePath().forEach(p -> builder.paths(PathSelectors.ant(p)));
        properties.getExcludeBasePath().forEach(p -> builder.paths(PathSelectors.ant(p).negate()));

        Docket docket = builder.build().pathMapping("/");
        // 如果开启认证
        if (properties.getAuthorization().getEnabled()) {
            docket.securitySchemes(Collections.singletonList(apiKey()));
            docket.securityContexts(Collections.singletonList(securityContext()));
        }

        return docket;
    }


    /**
     * apiInfo 配置文档基本信息
     * 如：文档标题、版本、描述、联系人基本信息等
     *
     * @author fxbin
     * @since 2020/4/1 11:23
     * @return springfox.documentation.service.ApiInfo
     */
    private ApiInfo apiInfo(){
        // 应用名
        String applicationName = environment.getProperty(APPLICATION_NAME);
        String defaultName = Optional.ofNullable(applicationName).orElse("default") + "服务";

        String title = Optional.ofNullable(properties.getTitle()).orElse(defaultName);
        String description = Optional.ofNullable(properties.getDescription()).orElse(defaultName);

        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .version(properties.getVersion())
                .license(properties.getLicense())
                .licenseUrl(properties.getLicenseUrl())
                .termsOfServiceUrl(properties.getTermsOfServiceUrl())
                .contact(new Contact(properties.getContact().getName(), properties.getContact().getUrl(), properties.getContact().getEmail()))
                .build();
    }


    /**
     * apiKey
     * 配置Swagger整合Oauth2时的请求Key信息
     * 使用头部传递方式 Authorization: TokenValue
     *
     * @author fxbin
     * @since 2020/4/1 11:24
     * @return springfox.documentation.service.ApiKey
     */
    private ApiKey apiKey() {
        return new ApiKey(properties.getAuthorization().getName(), properties.getAuthorization().getKeyName(), ApiKeyVehicle.HEADER.getValue());
    }


    /**
     * securityContext 配置安全上下文
     *
     * @author fxbin
     * @since 2020/4/1 11:41
     * @return springfox.documentation.spi.service.contexts.SecurityContext
     */
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(this.defaultAuth())
                .forPaths(PathSelectors.regex(properties.getAuthorization().getAuthRegex()))
                .build();
    }


    /**
     * defaultAuth 配置默认权限
     *
     * @author fxbin
     * @since 2020/4/1 11:41
     * @return java.util.List<springfox.documentation.service.SecurityReference>
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return Collections
                .singletonList(SecurityReference.builder().reference(properties.getAuthorization().getName())
                .scopes(authorizationScopes).build());
    }


}
