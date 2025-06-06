package cn.fxbin.bubble.plugin.satoken.autoconfigure;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * SaTokenProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 15:30
 */
@Data
@ConfigurationProperties(prefix = SaTokenProperties.BUBBLE_SATOKEN_PREFIX)
public class SaTokenProperties {

    /**
     * satoken prefix
     */
    public static final String BUBBLE_SATOKEN_PREFIX = "bubble.satoken";

    /**
     * 是否启用 Sa-Token
     */
    private boolean enabled = true;

    /**
     * JWT 配置
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * 认证相关配置
     */
    private Auth auth = new Auth();


    /**
     * JWT 配置类
     */
    @Data
    public static class JwtConfig {

        /**
         * 是否启用 JWT
         */
        private boolean enabled = false;

        /**
         * JWT 模式，可选值：simple、mixin、stateless
         * simple: JWT 不会存储到 Redis 中，只是单纯的 JWT 校验模式
         * mixin: JWT 会存储到 Redis 中，但是只是做 Token 有效性校验，不会校验 JWT 本身
         * stateless: JWT 不会存储到 Redis 中，只是单纯的 JWT 校验模式，但是会校验 JWT 本身
         */
        private JwtMode jwtMode = JwtMode.simple;
    }


    public enum JwtMode {
        simple, mixin, stateless
    }

    @Data
    public static class Auth {

        /**
         * 拦截url，默认
         */
        private List<String> includeUrls = Lists.newArrayList("/**");

        /**
         * 放行url
         */
        private List<String> excludeUrls = Lists.newArrayList();

    }

}