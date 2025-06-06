package cn.fxbin.bubble.plugin.satoken.autoconfigure;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.jwt.StpLogicJwtForMixin;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.fxbin.bubble.core.dataobject.GlobalErrorCode;
import cn.fxbin.bubble.core.dataobject.Result;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.plugin.satoken.exception.SaTokenExceptionHandler;
import cn.fxbin.bubble.plugin.satoken.filter.SaTokenContextFilter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * SaTokenAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 15:35
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = SaTokenProperties.BUBBLE_SATOKEN_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SaTokenProperties.class)
@Import({SaTokenExceptionHandler.class})
public class SaTokenAutoConfiguration {

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public SaTokenContextFilter saTokenFilter() {
        return new SaTokenContextFilter();
    }

    /**
     * JWT 整合（简单模式）
     */
    @Bean
    @ConditionalOnProperty(prefix = SaTokenProperties.BUBBLE_SATOKEN_PREFIX + ".jwt", name = {"enabled", "jwt-mode"}, havingValue = "simple")
    public StpLogic stpLogicJwtForSimple() {
        log.info("Sa-Token 集成 JWT 模式：simple");
        return new StpLogicJwtForSimple();
    }

    /**
     * JWT 整合（混入模式）
     */
    @Bean
    @ConditionalOnProperty(prefix = SaTokenProperties.BUBBLE_SATOKEN_PREFIX + ".jwt", name = {"enabled", "jwt-mode"}, havingValue = "mixin")
    public StpLogic stpLogicJwtForMixin() {
        log.info("Sa-Token 集成 JWT 模式：mixin");
        return new StpLogicJwtForMixin();
    }

    /**
     * JWT 整合（无状态模式）
     */
    @Bean
    @ConditionalOnProperty(prefix = SaTokenProperties.BUBBLE_SATOKEN_PREFIX + ".jwt", name = {"enabled", "jwt-mode"}, havingValue = "stateless")
    public StpLogic stpLogicJwtForStateless() {
        log.info("Sa-Token 集成 JWT 模式：stateless");
        return new StpLogicJwtForStateless();
    }

    private final List<String> DEFAULT_EXCLUDE_URLS = Lists.newArrayList(
            "/doc.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/favicon.ico",
            "/v3/api-docs/**",
            "/v3/api-docs/default",
            "/v3/api-docs/swagger-config"
    );

    /**
     * 注册 Sa-Token 全局Servlet过滤器
     * <p>
     * 此过滤器负责处理认证、授权、以及全局异常。
     * </p>
     *
     * @param properties Sa-Token 配置属性
     * @return {@link SaServletFilter} Sa-Token Servlet 过滤器实例
     */
    @Bean
    public SaServletFilter saServletFilter(SaTokenProperties properties) {

        // 补充默认放行API
        List<String> excludeUrls = properties.getAuth().getExcludeUrls();
        excludeUrls.addAll(DEFAULT_EXCLUDE_URLS);

        return new SaServletFilter()
                // 指定 [拦截路由]
                .setIncludeList(properties.getAuth().getIncludeUrls())
                // [放行路由]
                .setExcludeList(excludeUrls)

                // 认证函数: 每次请求执行
                .setAuth(obj -> {
                    SaRouter.match(properties.getAuth().getIncludeUrls())
                            .notMatch(excludeUrls)
                            .check(r -> {
                                if (!StpUtil.isLogin()) {
                                    throw new ServiceException(GlobalErrorCode.UNAUTHORIZED);
                                }
                            });
                })

                // 异常处理函数：每次认证函数发生异常时执行此函数
                .setError(e -> {
                    log.error("sa全局异常", e);
                    if (e instanceof SaTokenException) {
                        return JsonUtils.toJson(SaTokenExceptionHandler.handleEx((Exception) e));
                    }
                    return JsonUtils.toJson(Result.failure(e.getMessage()));
                })

                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(r -> {
                    // ---------- 设置一些安全响应头 ----------
                    SaHolder.getResponse()
                            // 服务器名称
                            // .setServer("bubble") // 一般由网关或Web服务器设置
                            // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            // 禁用浏览器内容嗅探
                            .setHeader("X-Content-Type-Options", "nosniff");

// === 注释 ===
//                            // 跨域部分配置
//                            // 允许指定域访问跨域资源
//                            .setHeader("Access-Control-Allow-Origin", "*")
//                            // 允许所有请求方式
//                            .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
//                            // 有效时间
//                            .setHeader("Access-Control-Max-Age", "3600")
//                            // 允许的header参数
//                            .setHeader("Access-Control-Allow-Headers", "*");
                });
    }

}