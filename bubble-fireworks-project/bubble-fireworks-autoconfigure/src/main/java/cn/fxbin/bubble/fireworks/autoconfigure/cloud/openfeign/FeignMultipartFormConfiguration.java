package cn.fxbin.bubble.fireworks.autoconfigure.cloud.openfeign;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * FeignMutipartConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/22 11:48
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({SpringFormEncoder.class})
public class FeignMultipartFormConfiguration {

    private final ObjectFactory<HttpMessageConverters> messageConverters;

    public FeignMultipartFormConfiguration(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Bean
    @Scope("prototype")
    @Primary
    public Encoder feignEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

}
