package cn.fxbin.bubble.fireworks.autoconfigure.cloud.openfeign;

import cn.fxbin.bubble.fireworks.cloud.feign.OkHttp3ConnectionManager;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FeignOkHttp3Configuration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/21 18:54
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass(OkHttp3ConnectionManager.class)
public class FeignOkHttp3Configuration {

    @Bean
    public OkHttpClient okHttpClient() {
        return OkHttp3ConnectionManager.createDefault();
    }


}
