package cn.fxbin.bubble.fireworks.autoconfigure.cloud.openfeign;

import cn.fxbin.bubble.fireworks.cloud.feign.CustomizeFeignErrorDecoder;
import cn.fxbin.bubble.fireworks.cloud.feign.OkHttp3ConnectionManager;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.*;

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

    private final ObjectFactory<HttpMessageConverters> messageConverters;

    public FeignGlobalConfiguration(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return OkHttp3ConnectionManager.createDefault();
    }

    @Bean
    @Scope("prototype")
    @Primary
    public Encoder feignEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }



}
