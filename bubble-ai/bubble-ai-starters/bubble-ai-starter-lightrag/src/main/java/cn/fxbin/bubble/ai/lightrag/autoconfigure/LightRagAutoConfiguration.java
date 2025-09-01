package cn.fxbin.bubble.ai.lightrag.autoconfigure;

import cn.fxbin.bubble.core.support.YamlConfigFactory;
import cn.hutool.core.date.SystemClock;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.ForestInterceptor;
import com.dtflys.forest.springboot.annotation.ForestScan;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
     * LightRAG 自动配置类
     * 
     * <p>该类负责自动配置 LightRAG 相关的 Bean，包括 Forest HTTP 客户端的配置、
     * 认证拦截器、超时设置、重试策略等。当满足特定条件时，会自动创建和配置相关组件。</p>
     * 
     * <h3>自动配置条件：</h3>
     * <ul>
     *   <li>类路径中存在 Forest 相关类</li>
     *   <li>配置属性 dm.ai.lightrag.enabled=true（默认为 true）</li>
     *   <li>配置了有效的 base-url</li>
     * </ul>
     * 
     * <h3>主要功能：</h3>
     * <ul>
     *   <li>配置 Forest HTTP 客户端的全局设置</li>
     *   <li>创建认证拦截器，支持API Key认证</li>
     *   <li>配置超时和重试参数</li>
     *   <li>提供 LightRAG 客户端 Bean 的自动装配</li>
     * </ul>
     * 
     * @author fxbin
     * @version v1.0
     * @since 2025-08-21 15:22:03
     */
@Slf4j
@Configuration(
        proxyBeanMethods = false
)
@PropertySource(value = "classpath:lightrag-forest.yaml", factory = YamlConfigFactory.class)
@RequiredArgsConstructor
@ForestScan(basePackages = "cn.fxbin.bubble.ai.lightrag.client")
@ConditionalOnClass(com.dtflys.forest.Forest.class)
@EnableConfigurationProperties(LightRagProperties.class)
@ConditionalOnProperty(prefix = "bubble.ai.lightrag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LightRagAutoConfiguration {

    private final LightRagProperties properties;

    /**
     * 配置 Forest 全局属性
     *
     * <p>通过 Spring Boot 配置属性的方式配置 Forest 全局参数。
     * 使用 @ForestScan 注解后，Forest 会自动读取这些配置。</p>
     */
    @PostConstruct
    public void configureForest() {
        log.info("正在配置 LightRAG Forest 客户端，基础 URL: {}", properties.getBaseUrl());
        log.info("LightRAG Forest 客户端配置完成，使用 @ForestScan 自动扫描客户端接口");
    }

    /**
     * 创建认证拦截器
     *
     * <p>创建API Key认证拦截器，支持通过API Key进行认证。</p>
     *
     * @return 认证拦截器
     */
    @Bean
    @ConditionalOnMissingBean(name = "lightRagAuthInterceptor")
    public ForestInterceptor lightRagAuthInterceptor() {
        return new LightRagAuthInterceptor(properties);
    }

    /**
     * 创建日志拦截器
     *
     * <p>用于记录 HTTP 请求和响应的详细信息，便于调试和监控。</p>
     *
     * @return 日志拦截器
     */
    @Bean
    @ConditionalOnMissingBean(name = "lightRagLoggingInterceptor")
    public ForestInterceptor lightRagLoggingInterceptor() {
        return new LightRagLoggingInterceptor();
    }

    /**
     * LightRAG 认证拦截器
     *
     * <p>实现API Key认证的 HTTP 请求拦截和认证信息添加。</p>
     */
    @RequiredArgsConstructor
    public static class LightRagAuthInterceptor implements ForestInterceptor {

        private final LightRagProperties properties;

        @Override
        public boolean beforeExecute(ForestRequest request) {
            String apiKey = properties.getApiKey();
            if (StringUtils.hasText(apiKey)) {
                // Add API key as query parameter
                request.addQuery("api_key_header_value", apiKey);
            }
            return true;
        }
    }

    /**
     * LightRAG 日志拦截器
     *
     * <p>记录 HTTP 请求和响应的详细信息，包括请求 URL、方法、参数、响应状态码、耗时等。</p>
     */
    public static class LightRagLoggingInterceptor implements ForestInterceptor {

        @Override
        public boolean beforeExecute(ForestRequest request) {
            if (log.isDebugEnabled()) {
                log.debug("LightRAG 请求开始: {} {}", request.getType().getName(), request.getUrl());
                if (request.getQuery() != null && !request.getQuery().isEmpty()) {
                    log.debug("请求参数: {}", request.getQuery());
                }
            }

            // 记录请求开始时间
            request.addAttachment("startTime", SystemClock.now());
            return true;
        }

        @Override
        public void afterExecute(ForestRequest request, ForestResponse response) {
            if (log.isDebugEnabled()) {
                log.debug("LightRAG 请求结束: {} {} - 状态码: {}",
                        request.getType().getName(), request.getUrl(), response.getStatusCode());
                if (response.getContent() != null) {
                    log.debug("响应内容: {}", response.getContent());
                }
            }
            log.debug("请求耗时: {}ms", SystemClock.now() - (Long) request.getAttachment("startTime"));
        }
    }

    /**
     * 内部配置类
     *
     * <p>包含需要在特定条件下才创建的 Bean 配置。</p>
     */
    @Configuration
    @ConditionalOnProperty(prefix = "dm.ai.lightrag", name = "base-url")
    static class LightRagClientConfiguration {

        /**
         * 验证配置的有效性
         *
         * <p>在应用启动时验证 LightRAG 配置是否正确，如果配置无效则记录警告信息。</p>
         *
         * @param properties LightRAG 配置属性
         */
        @Bean
        @ConditionalOnMissingBean
        public LightRagConfigurationValidator lightRagConfigurationValidator(LightRagProperties properties) {
            return new LightRagConfigurationValidator(properties);
        }
    }

    /**
     * LightRAG 配置验证器
     *
     * <p>用于验证 LightRAG 配置的有效性，确保必要的配置项都已正确设置。</p>
     */
    @RequiredArgsConstructor
    public static class LightRagConfigurationValidator {

        private final LightRagProperties properties;

        @PostConstruct
        public void validate() {
            log.info("开始验证 LightRAG 配置...");

            // 验证基础 URL
            if (!StringUtils.hasText(properties.getBaseUrl())) {
                log.warn("LightRAG 基础 URL 未配置，服务可能无法正常工作");
                return;
            }

            String apiKey = properties.getApiKey();
            if (!StringUtils.hasText(apiKey)) {
                log.warn("LightRAG 配置了 API Key 认证但未提供 apiKey");
            }

            int maxRetries = properties.getMaxRetries();
            if (maxRetries < 0) {
                log.warn("LightRAG 配置了无效的重试次数，将使用默认值 3");
            }
            if (maxRetries > 5) {
                log.warn("LightRAG 配置了超过 5 次的重试次数，这可能会导致性能问题");
            }

            Duration timeout = properties.getTimeout();
            if (timeout.isNegative()) {
                log.warn("LightRAG 配置了无效的超时时间，将使用默认值 10 秒");
            }

            log.info("LightRAG 配置验证完成");
        }
    }
}
