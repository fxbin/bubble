package cn.fxbin.bubble.fireworks.autoconfigure.plugin.token;

import cn.fxbin.bubble.plugin.token.DoubleJwt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

/**
 * TokenAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/14 19:06
 */
@Configuration(
        proxyBeanMethods = false
)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass({DoubleJwt.class})
@EnableConfigurationProperties(TokenProperties.class)
public class TokenAutoConfiguration {

    @Resource
    private TokenProperties tokenProperties;

    @Bean
    public DoubleJwt doubleJwt() {
        Long accessExpire = tokenProperties.getTokenAccessExpire();
        Long refreshExpire = tokenProperties.getTokenRefreshExpire();
        String secret = tokenProperties.getSecret();
        return new DoubleJwt(accessExpire, refreshExpire, secret);
    }


}

