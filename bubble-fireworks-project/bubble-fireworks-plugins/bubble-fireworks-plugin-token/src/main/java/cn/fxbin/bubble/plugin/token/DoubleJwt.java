package cn.fxbin.bubble.plugin.token;

import cn.fxbin.bubble.fireworks.core.util.BeanUtils;
import cn.fxbin.bubble.fireworks.core.util.CollectionUtils;
import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import cn.fxbin.bubble.fireworks.core.util.SystemClock;
import cn.fxbin.bubble.fireworks.core.util.time.DateUtils;
import cn.fxbin.bubble.plugin.token.constant.TokenConstants;
import cn.fxbin.bubble.plugin.token.exception.InvalidClaimException;
import cn.fxbin.bubble.plugin.token.exception.TokenExpiredException;
import cn.fxbin.bubble.plugin.token.model.TokenPayload;
import cn.fxbin.bubble.plugin.token.model.Tokens;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * DoubleJWT 双令牌模式
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/11 16:43
 */
public class DoubleJwt {

    /**
     * access 过期时间
     */
    private long accessExpire;

    /**
     * refresh 过期时间
     */
    private long refreshExpire;

    /**
     * 签名算法
     */
    private SignatureAlgorithm algorithm;

    /**
     * 用于签名的秘钥
     */
    private Key key;

    public DoubleJwt(long accessExpire, long refreshExpire, SignatureAlgorithm algorithm, Key key) {
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
        this.algorithm = algorithm;
        this.key = key;
    }

    public DoubleJwt(long accessExpire, long refreshExpire, SignatureAlgorithm algorithm) {
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
        this.algorithm = algorithm;
        this.key = Keys.secretKeyFor(algorithm);
    }

    public DoubleJwt(long accessExpire, long refreshExpire) {
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
        this.algorithm = SignatureAlgorithm.HS512;
        this.key = Keys.secretKeyFor(this.algorithm);
    }

    public String generateToken(String tokenType, String identity, String scope, long expire) {
        TokenPayload claims = TokenPayload.builder()
                .type(tokenType)
                .identity(identity)
                .scope(scope)
                .build();
        return generateToken(claims, expire);
    }

    public String generateToken(String tokenType, long identity, String scope, long expire) {
        TokenPayload claims = TokenPayload.builder()
                .type(tokenType)
                .identity(String.valueOf(identity))
                .scope(scope)
                .build();
        return generateToken(claims, expire);
    }

    public String generateToken(String tokenType, long identity, String scope, long expire, Map<String, Object> extra) {
        TokenPayload claims = TokenPayload.builder()
                .type(tokenType)
                .identity(String.valueOf(identity))
                .scope(scope)
                .extra(extra)
                .build();
        return generateToken(claims, expire);
    }

    @SuppressWarnings("DuplicatedCode")
    public String generateToken(TokenPayload tokenPayload, long expire) {
        Map<String, Object> extra = tokenPayload.getExtra();
        tokenPayload.setExtra(null);
        Map<String, Object> claims = BeanUtils.object2Map(tokenPayload);
        if (CollectionUtils.isNotEmpty(extra)) {
            extra.keySet().forEach(key -> claims.put(key, extra.get(key)));
        }
        Date now = DateUtils.toDate(SystemClock.INSTANCE.currentTimeMillis());
        Date expireDate = DateUtils.toDate(now.getTime() + expire * 1000);

        return Jwts.builder()
                .setId(StringUtils.getUUID())
                .setHeaderParam("typ", "JWT")
                .setSubject(String.valueOf(tokenPayload.getIdentity()))
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key)
                .compact();
    }

    /**
     * parseAccessToken
     *
     * <p>
     *     Jwt Token 解析, jjwt-impl 会将毫秒值转化为秒
     *     {@link io.jsonwebtoken.impl.JwtMap#setDateAsSeconds(java.lang.String, java.util.Date)}
     * </p>
     *
     * @author fxbin
     * @since 2020/11/13 16:45
     * @param token jwt token
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public TokenPayload parseAccessToken(String token) {
        Map<String, Object> mapObj = (Map<String, Object>) Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parse(token)
                .getBody();

        TokenPayload payload = BeanUtils.map2Object(mapObj, TokenPayload.class);
        checkTokenExpired(payload.getExp());
        checkTokenScope(payload.getScope());
        checkTokenType(payload.getType(), TokenConstants.ACCESS_TYPE);
        return payload;
    }

    /**
     * parseAccessToken
     *
     * <p>
     *     Jwt Token 解析, jjwt-impl 会将毫秒值转化为秒
     *     {@link io.jsonwebtoken.impl.JwtMap#setDateAsSeconds(java.lang.String, java.util.Date)}
     * </p>
     *
     * @author fxbin
     * @since 2020/11/13 16:45
     * @param token jwt token
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public TokenPayload parseRefreshToken(String token) {
        Map<String, Object> mapObj = (Map<String, Object>) Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parse(token)
                .getBody();

        TokenPayload payload = BeanUtils.map2Object(mapObj, TokenPayload.class);
        checkTokenExpired(payload.getExp());
        checkTokenScope(payload.getScope());
        checkTokenType(payload.getType(), TokenConstants.REFRESH_TYPE);
        return payload;
    }

    /**
     * checkTokenExpired
     *
     * @author fxbin
     * @since 2020/11/14 18:57
     * @param exp 过期时间/秒
     */
    private void checkTokenExpired(Integer exp) {
        long nowSeconds = SystemClock.INSTANCE.currentTimeMillis() / 1000;
        if (nowSeconds > exp.longValue()) {
            throw new TokenExpiredException("token is expired");
        }
    }

    /**
     * checkTokenScope
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param scope 请求标识码
     */
    private void checkTokenScope(String scope) {
        this.checkTokenScope(scope, TokenConstants.BUBBLE_FIREWORKS_SCOPE);
    }

    /**
     * checkTokenScope
     *
     * @author fxbin
     * @since 2020/11/14 18:59
     * @param scope 请求标识码
     * @param certScope 认证标识码
     */
    private void checkTokenScope(String scope, String certScope) {
        if (scope == null || !scope.equals(certScope)) {
            throw new InvalidClaimException("token scope is invalid");
        }
    }


    /**
     * checkTokenType
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param type token 类型
     * @param accessType 认证token 类型
     */
    private void checkTokenType(String type, String accessType) {
        if (type == null || !type.equals(accessType)) {
            throw new InvalidClaimException("token type is invalid");
        }
    }

    /**
     * generateAccessToken
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param identity 身份标识
     * @return java.lang.String
     */
    public String generateAccessToken(long identity) {
        return generateToken(TokenConstants.ACCESS_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.accessExpire);
    }

    /**
     * generateAccessToken
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param identity 身份标识
     * @return java.lang.String
     */
    public String generateAccessToken(String identity) {
        return generateToken(TokenConstants.ACCESS_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.accessExpire);
    }

    /**
     * generateRefreshToken
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param identity 身份标识
     * @return java.lang.String
     */
    public String generateRefreshToken(long identity) {
        return generateToken(TokenConstants.REFRESH_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.refreshExpire);
    }

    /**
     * generateRefreshToken
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param identity 身份标识
     * @return java.lang.String
     */
    public String generateRefreshToken(String identity) {
        return generateToken(TokenConstants.REFRESH_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.refreshExpire);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/14 18:56
     * @param identity 身份标识
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(long identity) {
        String access = this.generateToken(TokenConstants.ACCESS_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.accessExpire);
        String refresh = this.generateToken(TokenConstants.REFRESH_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.refreshExpire);
        return new Tokens(access, refresh);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/14 18:55
     * @param identity 身份标识
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(String identity) {
        String access = this.generateToken(TokenConstants.ACCESS_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.accessExpire);
        String refresh = this.generateToken(TokenConstants.REFRESH_TYPE, identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, this.refreshExpire);
        return new Tokens(access, refresh);
    }

    /**
     * getAlgorithm
     *
     * @author fxbin
     * @since 2020/11/14 18:55
     * @return io.jsonwebtoken.SignatureAlgorithm
     */
    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * getKey
     *
     * @author fxbin
     * @since 2020/11/14 18:55
     * @return java.security.Key
     */
    public Key getKey() {
        return key;
    }
}
