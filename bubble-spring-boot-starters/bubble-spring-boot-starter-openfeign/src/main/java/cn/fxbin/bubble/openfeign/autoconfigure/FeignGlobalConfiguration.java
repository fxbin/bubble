package cn.fxbin.bubble.openfeign.autoconfigure;

import cn.fxbin.bubble.openfeign.FeignProperties;
import cn.fxbin.bubble.openfeign.OkHttp3ConnectionManager;
import cn.fxbin.bubble.openfeign.codec.CustomizeFeignErrorDecoder;
import cn.fxbin.bubble.openfeign.handler.CustomizeUrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import feign.Logger;
import feign.Request;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * FeignGlobalConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/12/11 17:49
 */
@Configuration(
        proxyBeanMethods = false
)
@Import(CustomizeFeignErrorDecoder.class)
@EnableConfigurationProperties(FeignProperties.class)
@ConditionalOnClass({SpringFormEncoder.class, OkHttp3ConnectionManager.class})
public class FeignGlobalConfiguration {

    @Resource
    private FeignProperties properties;

    @Bean
    public OkHttpClient okHttpClient() {
        return OkHttp3ConnectionManager.createDefault();
    }

    @Bean
    @ConditionalOnMissingBean
    public BlockExceptionHandler blockExceptionHandler() {
        return new CustomizeUrlBlockHandler();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return properties.getLoggingLevel();
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(properties.getConnectTimeout(), TimeUnit.MILLISECONDS, properties.getReadTimeout(), TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
        return new SpringFormEncoder(new SpringEncoder(converters));
    }


}
