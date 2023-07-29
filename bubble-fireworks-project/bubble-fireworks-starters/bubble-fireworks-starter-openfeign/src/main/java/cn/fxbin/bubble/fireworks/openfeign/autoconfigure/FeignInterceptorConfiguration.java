package cn.fxbin.bubble.fireworks.openfeign.autoconfigure;

import cn.fxbin.bubble.fireworks.openfeign.FeignProperties;
import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static cn.fxbin.bubble.fireworks.openfeign.FeignProperties.ALLOW_HEADERS;

/**
 * FeignInterceptorConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/22 18:08
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({RequestInterceptor.class})
@EnableConfigurationProperties(FeignProperties.class)
public class FeignInterceptorConfiguration {

    private static Logger LOGGER = LoggerFactory.getLogger(FeignInterceptorConfiguration.class);

    @Resource
    private FeignProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor requestInterceptor() {

        List<String> allowHeaders = properties.getAllowHeaders();
        allowHeaders.addAll(Arrays.asList(ALLOW_HEADERS));

        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();

            if (ObjectUtils.isNotEmpty(attributes)) {
                HttpServletRequest request = attributes.getRequest();

                Enumeration<String> headerNames = request.getHeaderNames();
                if (ObjectUtils.isNotEmpty(headerNames)) {
                    while (headerNames.hasMoreElements()) {
                        String key = headerNames.nextElement();

                        // 仅支持配置的 header
                        allowHeaders.stream().filter(header -> header.equalsIgnoreCase(key) && StringUtils.isNotBlank(request.getHeader(key)))
                                .forEach(header -> {
                                    String value = request.getHeader(key);
                                    requestTemplate.header(key, value);

                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("feign header penetrate key :[{}], value: [{}]", key, value);
                                    }

                                });
                    }
                }
            }
        };
    }

}
