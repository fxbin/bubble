package cn.fxbin.bubble.fireworks.autoconfigure.plugin.token;

import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import cn.fxbin.bubble.plugin.token.DoubleJwt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.token.TokenProperties.BUBBLE_FIREWORKS_TOKEN_PREFIX;

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
        if (ObjectUtils.isEmpty(accessExpire)) {
            // 1 小时
            accessExpire = 60 * 60L;
        }
        if (ObjectUtils.isEmpty(refreshExpire)) {
            // 30 天
            refreshExpire = 60 * 60 * 24 * 30L;
        }
        return new DoubleJwt(accessExpire, refreshExpire);
    }


}

