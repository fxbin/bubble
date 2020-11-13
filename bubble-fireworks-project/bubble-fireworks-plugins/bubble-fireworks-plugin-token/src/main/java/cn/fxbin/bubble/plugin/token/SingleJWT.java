package cn.fxbin.bubble.plugin.token;


import cn.fxbin.bubble.fireworks.core.util.BeanUtils;
import cn.fxbin.bubble.fireworks.core.util.CollectionUtils;
import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import cn.fxbin.bubble.fireworks.core.util.time.DateUtils;
import cn.fxbin.bubble.plugin.token.model.TokenClaims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * SingleJWT 单令牌模式
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/11 16:43
 */
public class SingleJWT {

    /**
     * 过期时间
     */
    private long expire;

    /**
     * 签名算法
     */
    private SignatureAlgorithm algorithm;

    /**
     * 用于签名的秘钥
     */
    private Key key;


    public SingleJWT(long expire, SignatureAlgorithm algorithm, Key key) {
        this.expire = expire;
        this.algorithm = algorithm;
        this.key = key;
    }

    public SingleJWT(long expire) {
        this.expire = expire;
        this.algorithm = SignatureAlgorithm.HS512;
        this.key = Keys.secretKeyFor(this.algorithm);
    }

    public String generateToken(String tokenType, long identity, String scope, long expire, Map<String, Object> extra) {
        this.expire = expire;
        TokenClaims claims = TokenClaims.builder()
                .type(tokenType)
                .identity(String.valueOf(identity))
                .scope(scope)
                .extra(extra)
                .build();
        return generateToken(claims);
    }

    public String generateToken(TokenClaims tokenClaims) {
        Map<String, Object> extra = tokenClaims.getExtra();
        tokenClaims.setExtra(null);
        Map<String, Object> claims = BeanUtils.object2Map(tokenClaims);
        if (CollectionUtils.isNotEmpty(extra)) {
            extra.keySet().forEach(key -> claims.put(key, extra.get(key)));
        }

        Date now = DateUtils.toDate(DateUtils.localDateTime());
        Date expireDate = DateUtils.toDate(now.getTime() + expire);

        return Jwts.builder()
                .setId(StringUtils.getUUID())
                .setHeaderParam("typ", "JWT")
                .setSubject(String.valueOf(tokenClaims.getIdentity()))
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key)
                .compact();
    }

    /**
     * parseToken
     *
     * <p>
     *     Jwt Token 解析
     * </p>
     *
     * @author fxbin
     * @since 2020/11/13 16:45
     * @param token jwt token
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseToken(String token) {
        return (Map<String, Object>) Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parse(token)
                .getBody();
    }

    /**
     * getSignatureAlgorithm
     *
     * @author fxbin
     * @since 2020/11/11 16:56
     * @return io.jsonwebtoken.SignatureAlgorithm
     */
    public SignatureAlgorithm getSignatureAlgorithm() {
        return algorithm;
    }

    /**
     * getExpire
     *
     * @author fxbin
     * @since 2020/11/11 16:56
     * @return java.lang.Long
     */
    public Long getExpire() {
        return expire;
    }
}
