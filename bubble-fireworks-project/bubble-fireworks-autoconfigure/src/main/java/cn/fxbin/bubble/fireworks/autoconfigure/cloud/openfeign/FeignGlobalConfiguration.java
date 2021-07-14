package cn.fxbin.bubble.fireworks.autoconfigure.cloud.openfeign;

import cn.fxbin.bubble.fireworks.cloud.feign.OkHttp3ConnectionManager;
import cn.fxbin.bubble.fireworks.cloud.feign.codec.CustomizeFeignErrorDecoder;
import cn.fxbin.bubble.fireworks.cloud.feign.handler.CustomizeUrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import feign.Logger;
import feign.Request;
import feign.form.spring.SpringFormEncoder;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
@ConditionalOnClass({SpringFormEncoder.class, OkHttp3ConnectionManager.class})
public class FeignGlobalConfiguration {

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
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(2000, TimeUnit.MILLISECONDS, 2000, TimeUnit.MILLISECONDS, true);
    }


}
