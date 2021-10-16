package cn.fxbin.bubble.plugin.token;

import cn.fxbin.bubble.fireworks.core.util.*;
import cn.fxbin.bubble.fireworks.core.util.time.DateUtils;
import cn.fxbin.bubble.plugin.token.constant.TokenConstants;
import cn.fxbin.bubble.plugin.token.exception.InvalidClaimException;
import cn.fxbin.bubble.plugin.token.exception.TokenExpiredException;
import cn.fxbin.bubble.plugin.token.model.TokenPayload;
import cn.fxbin.bubble.plugin.token.model.Tokens;
import cn.hutool.core.date.SystemClock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
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

    /**
     * 秘钥
     */
    private String secret;


    public DoubleJwt(long accessExpire, long refreshExpire, SignatureAlgorithm algorithm, Key key, String secret) {
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
        this.algorithm = algorithm;
        this.key = key;
        this.secret = secret;
    }

    public DoubleJwt(long accessExpire, long refreshExpire, SignatureAlgorithm algorithm, String secret) {
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
        this.algorithm = algorithm;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public DoubleJwt(long accessExpire, long refreshExpire, String secret) {
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
        this.algorithm = SignatureAlgorithm.HS512;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
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
        return generateToken(tokenType, String.valueOf(identity), scope, expire, extra);
    }

    public String generateToken(String tokenType, String identity, String scope, long expire, Map<String, Object> extra) {
        TokenPayload claims = TokenPayload.builder()
                .type(tokenType)
                .identity(identity)
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
            extra.keySet().stream().filter(key -> ObjectUtils.isNotEmpty(extra.get(key)))
                    .forEach(key -> claims.put(key, extra.get(key)));
        }
        Date now = DateUtils.toDate(SystemClock.now());
        Date expireDate = DateUtils.toDate(now.getTime() + expire * 1000);

        return Jwts.builder()
                .setId(StringUtils.getUUID())
                .setHeaderParam("typ", "JWT")
                .setSubject(String.valueOf(tokenPayload.getIdentity()))
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, algorithm)
                .compact();
    }

    /**
     * parseToken
     *
     * @author fxbin
     * @since 2020/11/18 19:35
     * @param token jwt token
     * @return cn.fxbin.bubble.plugin.token.model.TokenPayload
     */
    public TokenPayload parseToken(String token) {
        Map<String, Object> mapObj = (Map<String, Object>) Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
 
        TokenPayload payload = BeanUtils.map2Object(mapObj, TokenPayload.class);
        checkTokenExpired(payload.getExp());
        return payload;
    }

    /**
     * parseAccessToken
     *
     * @author fxbin
     * @since 2020/11/18 18:58
     * @param token jwt token
     * @return cn.fxbin.bubble.plugin.token.model.TokenPayload
     */
    public TokenPayload parseAccessToken(String token) {
        return this.parseAccessToken(token, TokenConstants.BUBBLE_FIREWORKS_SCOPE);
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
     * @param scope scope
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @SuppressWarnings({"DuplicatedCode"})
    public TokenPayload parseAccessToken(String token, String scope) {
        TokenPayload payload = parseToken(token);
        checkTokenScope(payload.getScope(), scope);
        checkTokenType(payload.getType(), TokenConstants.ACCESS_TYPE);
        return payload;
    }

    /**
     * parseRefreshToken
     *
     * @author fxbin
     * @since 2020/11/18 18:57
     * @param token jwt token
     * @return cn.fxbin.bubble.plugin.token.model.TokenPayload
     */
    public TokenPayload parseRefreshToken(String token) {
       return this.parseRefreshToken(token, TokenConstants.BUBBLE_FIREWORKS_SCOPE);
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
     * @param scope scope
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @SuppressWarnings({"DuplicatedCode"})
    public TokenPayload parseRefreshToken(String token, String scope) {
        TokenPayload payload = parseToken(token);
        checkTokenScope(payload.getScope(), scope);
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
        long nowSeconds = SystemClock.now() / 1000;
        if (nowSeconds > exp.longValue()) {
            throw new TokenExpiredException("token is expired");
        }
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
        return this.generateTokens(String.valueOf(identity));
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
        return this.generateTokens(identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/19 16:58
     * @param identity 身份标识
     * @param scope 请求标识码
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(long identity, String scope) {
        return this.generateTokens(String.valueOf(identity), scope, null);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/18 18:52
     * @param identity 身份标识
     * @param scope 请求标识码
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(String identity, String scope) {
        return this.generateTokens(identity, scope, null);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/19 16:57
     * @param identity 身份标识
     * @param extra 额外扩展信息
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(long identity, Map<String, Object> extra) {
        return this.generateTokens(String.valueOf(identity), TokenConstants.BUBBLE_FIREWORKS_SCOPE, extra);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/19 10:19
     * @param identity 身份标识
     * @param extra 额外扩展信息
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(String identity, Map<String, Object> extra) {
        return this.generateTokens(identity, TokenConstants.BUBBLE_FIREWORKS_SCOPE, extra);
    }

    /**
     * generateTokens
     *
     * @author fxbin
     * @since 2020/11/18 19:08
     * @param identity 身份标识
     * @param scope 请求标识码
     * @param extra 额外扩展信息
     * @return cn.fxbin.bubble.plugin.token.model.Tokens
     */
    public Tokens generateTokens(String identity, String scope, Map<String, Object> extra) {
        String access = this.generateToken(TokenConstants.ACCESS_TYPE, identity, scope, this.accessExpire, extra);
        String refresh = this.generateToken(TokenConstants.REFRESH_TYPE, identity, scope, this.refreshExpire, extra);
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
