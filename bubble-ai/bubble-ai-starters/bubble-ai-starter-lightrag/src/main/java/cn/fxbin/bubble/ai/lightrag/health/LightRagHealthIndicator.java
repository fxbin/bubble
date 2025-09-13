package cn.fxbin.bubble.ai.lightrag.health;

import cn.fxbin.bubble.ai.lightrag.autoconfigure.LightRagProperties;
import cn.fxbin.bubble.ai.lightrag.client.LightRagDefaultClient;
import cn.fxbin.bubble.ai.lightrag.util.SecurityUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * LightRAG 服务健康检查指示器
 * 
 * 该组件用于监控 LightRAG 服务的健康状态，包括文档管理、查询和图管理三个核心模块的可用性。
 * 通过并行检查各个客户端的健康状态，提供全面的服务健康评估。
 * 
 * 健康检查包括：
 * - 服务健康状态
 * - 响应时间统计
 * - 服务配置信息
 * 
 * @author fxbin
 * @since 2024-12-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LightRagHealthIndicator implements HealthIndicator {

    private final LightRagProperties properties;

    private final LightRagDefaultClient defaultClient;

    /**
     * 执行健康检查
     * 
     * 并行检查所有 LightRAG 客户端的健康状态，收集响应时间和状态信息。
     * 如果所有服务都正常，返回 UP 状态；如果任何服务异常，返回 DOWN 状态。
     * 
     * @return 健康检查结果，包含详细的状态信息和响应时间
     */
    @Override
    public Health health() {
        Instant startTime = Instant.now();
        
        try {
            // 并行执行健康检查
            CompletableFuture<HealthCheckResult> serverHealth = checkHealth();

            // 等待所有检查完成，设置超时时间
            CompletableFuture<Void> allChecks = CompletableFuture.allOf(
                    serverHealth
            );
            
            allChecks.get(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            
            // 收集检查结果
            HealthCheckResult serverResult = serverHealth.get();

            Duration totalTime = Duration.between(startTime, Instant.now());
            
            // 构建健康状态详情
            Map<String, Object> details = buildHealthDetails(
                serverResult, totalTime
            );
            
            // 判断整体健康状态
            boolean isHealthy = serverResult.isHealthy();
            
            return isHealthy ? Health.up().withDetails(details).build() 
                            : Health.down().withDetails(details).build();
            
        } catch (Exception e) {
            log.error("LightRAG 健康检查执行失败", e);
            Duration totalTime = Duration.between(startTime, Instant.now());
            
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getMessage());
            details.put("checkTime", totalTime.toMillis() + "ms");
            details.put("timestamp", Instant.now().toString());
            addConfigurationDetails(details);
            
            return Health.down().withDetails(details).build();
        }
    }
    
    /**
     * 检查文档管理服务健康状态
     * 
     * @return 文档服务健康检查结果的异步任务
     */
    private CompletableFuture<HealthCheckResult> checkHealth() {
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            try {
                defaultClient.healthCheck();
                Duration responseTime = Duration.between(start, Instant.now());
                return new HealthCheckResult(true, "Document service is healthy", responseTime);
            } catch (Exception e) {
                Duration responseTime = Duration.between(start, Instant.now());
                log.warn("文档服务健康检查失败: {}", e.getMessage());
                return new HealthCheckResult(false, "Document service error: " + e.getMessage(), responseTime);
            }
        });
    }
    

    
    /**
     * 构建健康状态详细信息
     * 
     * @param serverResult 服务器服务检查结果
     * @param totalTime 总检查时间
     * @return 包含详细健康信息的映射
     */
    private Map<String, Object> buildHealthDetails(
            HealthCheckResult serverResult, Duration totalTime) {
        
        Map<String, Object> details = new HashMap<>();
        
        // 服务状态信息
        Map<String, Object> services = new HashMap<>();
        services.put("server", buildServiceDetails(serverResult));
        details.put("services", services);
        
        // 性能统计
        Map<String, Object> performance = new HashMap<>();
        performance.put("totalCheckTime", totalTime.toMillis() + "ms");
        performance.put("averageResponseTime", 
            serverResult.getResponseTime().toMillis() + "ms");
        details.put("performance", performance);
        
        // 时间戳和配置信息
        details.put("timestamp", Instant.now().toString());
        addConfigurationDetails(details);
        
        return details;
    }
    
    /**
     * 构建单个服务的详细信息
     * 
     * @param result 健康检查结果
     * @return 服务详细信息映射
     */
    private Map<String, Object> buildServiceDetails(HealthCheckResult result) {
        Map<String, Object> serviceDetails = new HashMap<>();
        serviceDetails.put("status", result.isHealthy() ? "UP" : "DOWN");
        serviceDetails.put("message", result.getMessage());
        serviceDetails.put("responseTime", result.getResponseTime().toMillis() + "ms");
        return serviceDetails;
    }
    
    /**
     * 添加配置相关的详细信息
     * 
     * 对配置信息进行安全处理，确保敏感信息（如API密钥）不会在健康检查结果中泄露。
     * 使用脱敏处理保护敏感数据的同时，仍然提供足够的信息用于问题诊断。
     * 
     * @param details 详细信息映射
     */
    private void addConfigurationDetails(Map<String, Object> details) {
        Map<String, Object> config = new HashMap<>();
        config.put("baseUrl", SecurityUtils.maskUrl(properties.getBaseUrl()));
        config.put("maxRetries", properties.getMaxRetries());
        config.put("apiKey", SecurityUtils.maskApiKey(properties.getApiKey()));
        config.put("timeout", properties.getTimeout().toString());
        
        // 应用敏感信息过滤机制
        Map<String, Object> secureConfig = SecurityUtils.maskSensitiveConfig(config);
        details.put("configuration", secureConfig);
    }
    
    /**
     * 健康检查结果内部类
     * 
     * 封装单个服务的健康检查结果，包括健康状态、消息和响应时间。
     */
    @Getter
    @RequiredArgsConstructor
    private static class HealthCheckResult {

        /**
         * 健康状态
         */
        private final boolean healthy;
        /**
         * 健康检查消息
         */
        private final String message;
        /**
         * 响应时间
         */
        private final Duration responseTime;
    }
}