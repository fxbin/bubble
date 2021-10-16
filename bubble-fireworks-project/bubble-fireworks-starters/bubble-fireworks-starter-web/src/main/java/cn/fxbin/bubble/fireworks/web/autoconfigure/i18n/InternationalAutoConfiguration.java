package cn.fxbin.bubble.fireworks.web.autoconfigure.i18n;

import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * I18nConfigure
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/2 10:35
 * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({LocaleChangeInterceptor.class, LocalValidatorFactoryBean.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InternationalAutoConfiguration implements WebMvcConfigurer {

    @Resource
    private Environment environment;

    /**
     * Default name of the locale specification parameter: "locale".
     */
    private static final String DEFAULT_PARAM_NAME = "lang";

    private static final String SPRING_MESSAGES_BASENAME = "spring.messages.basename";

    @Bean
    public LocaleResolver localeResolver() {

        /*
         * AcceptHeaderLocaleResolver:Spring 采用的默认区域解析器是 AcceptHeaderLocaleResolver。它通过检验 HTTP 请求的 Accept-Language 头部来解析区域。这个头部是由用户的 Web 浏览器根据底层操作系统的区域设置进行设定或者 Web 端设定。
         * CookieLocaleResolver:Cookie 区域解析器，检查客户端中的 Cookie 是否包含本地化信息，如果有的话就使用。当配置这个解析器的时候，可以指定 Cookie 名，以及 Cookie 的最长生存期。
         * SessionLocaleResolver:Session 区域解析器，会从用户请求相关的 Session 中获取本地化信息。如果不存在，它会根据 Accept-Language 头部确定默认区域，只针对当前的会话有效，Session 失效，还原为默认状态。
         * FixedLocaleResolver:固定区域解析器，一直使用固定的 Local, 改变 Local 是不支持的，一旦程序启动时设定，则无法改变 Local
         */


        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setCookieName(DEFAULT_PARAM_NAME);
        //设置默认区域
        // localeResolver.setDefaultLocale(Locale.US);
        localeResolver.setCookieMaxAge(-1);

        return localeResolver;
    }

    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName(DEFAULT_PARAM_NAME);
        return localeChangeInterceptor;
    }

    public ResourceBundleMessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        //指定国际化的Resource Bundle地址
        resourceBundleMessageSource.setBasename(StringUtils.isNotBlank(environment.getProperty(SPRING_MESSAGES_BASENAME)) ? Objects.requireNonNull(environment.getProperty(SPRING_MESSAGES_BASENAME)) : "messages");
        //指定国际化的默认编码
        resourceBundleMessageSource.setDefaultEncoding("UTF-8");
        return resourceBundleMessageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(responseBodyConverter());
    }

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");
        validator.setValidationMessageSource(resourceBundleMessageSource());
        return validator;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        return new DefaultMessageCodesResolver();
    }
}
