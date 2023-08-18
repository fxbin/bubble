package cn.fxbin.bubble.plugin.token.autoconfigure;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.security.Key;

import static cn.fxbin.bubble.plugin.token.autoconfigure.TokenProperties.BUBBLE_FIREWORKS_TOKEN_PREFIX;

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

    public static final String BUBBLE_FIREWORKS_TOKEN_PREFIX = "bubble.token";

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
     * 秘钥，不建议使用默认值
     */
    private String secret = "CnNzZqcjvdU4IaQuDgpbJP8V193BKHwYOReMTtAEfXoGSl6rs7FLkx2y5ihm0WR5PwDlgxe1iA8hC1QnLWhFswCubF88eNk4vG8V3FY5ASu1al3PNBaolIGy5r1lRFLL";

    /**
     * 签名算法
     */
    private SignatureAlgorithm algorithm = SignatureAlgorithm.HS512;

    /**
     * 用于签名的秘钥
     */
    private Key key = Keys.secretKeyFor(getAlgorithm());

}
