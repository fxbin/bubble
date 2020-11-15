package cn.fxbin.bubble.fireworks.autoconfigure.plugin.token;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.security.Key;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.token.TokenProperties.BUBBLE_FIREWORKS_TOKEN_PREFIX;

/**
 * TokenProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/14 19:01
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = BUBBLE_FIREWORKS_TOKEN_PREFIX)
public class TokenProperties {

    public static final String BUBBLE_FIREWORKS_TOKEN_PREFIX = "bubble.fireworks.token";

    /**
     * 是否开启 token，默认：true
     */
    private boolean enabled = true;

    /**
     * access token 过期时间, 默认 1小时
     */
    private Long tokenAccessExpire = 3600L;

    /**
     * refresh token 过期时间， 默认 30天
     */
    private Long tokenRefreshExpire = 2592000L;

    /**
     * 签名算法
     */
    private SignatureAlgorithm algorithm = SignatureAlgorithm.HS512;

    /**
     * 用于签名的秘钥
     */
    private Key key = Keys.secretKeyFor(getAlgorithm());

}