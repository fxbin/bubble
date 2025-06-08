package cn.fxbin.bubble.data.redis.autoconfigure;

import cn.fxbin.bubble.data.redis.RedissonOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Redisson 自动配置类
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/1/1 00:00
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = BubbleRedisProperties.Redisson.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties({BubbleRedisProperties.class, RedisProperties.class})
@RequiredArgsConstructor
public class RedissonAutoConfiguration {

    private final BubbleRedisProperties bubbleRedisProperties;
    private final RedisProperties redisProperties;
    private final ResourceLoader resourceLoader;

    /**
     * 创建 RedissonClient Bean
     *
     * @return RedissonClient
     */
    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient() {
        BubbleRedisProperties.Redisson redissonProperties = bubbleRedisProperties.getRedisson();
        
        Config config;
        if (StringUtils.hasText(redissonProperties.getConfigLocation())) {
            try {
                config = loadConfigFromFile(redissonProperties.getConfigLocation());
            } catch (IOException e) {
                log.warn("Failed to load Redisson config from file: {}, using default config", 
                        redissonProperties.getConfigLocation(), e);
                config = createDefaultConfig();
            }
        } else {
            config = createDefaultConfig();
        }
        
        // 应用自定义配置
        applyCustomConfig(config, redissonProperties);
        
        return Redisson.create(config);
    }

    /**
     * 创建 RedissonOperations Bean
     *
     * @param redissonClient RedissonClient
     * @return RedissonOperations
     */
    @Bean
    @ConditionalOnMissingBean
    public RedissonOperations redissonOperations(RedissonClient redissonClient) {
        return new RedissonOperations(redissonClient);
    }

    /**
     * 从文件加载配置
     *
     * @param configLocation 配置文件位置
     * @return Config
     * @throws IOException IO异常
     */
    private Config loadConfigFromFile(String configLocation) throws IOException {
        Resource resource = resourceLoader.getResource(configLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            if (configLocation.endsWith(".json")) {
                return Config.fromJSON(inputStream);
            } else if (configLocation.endsWith(".yaml") || configLocation.endsWith(".yml")) {
                return Config.fromYAML(inputStream);
            } else {
                throw new IllegalArgumentException("Unsupported config file format: " + configLocation);
            }
        }
    }

    /**
     * 创建默认配置
     *
     * @return Config
     */
    private Config createDefaultConfig() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        
        // 构建Redis地址
        String address = String.format("redis://%s:%d", 
                redisProperties.getHost(), redisProperties.getPort());
        singleServerConfig.setAddress(address);
        
        // 设置密码
        if (StringUtils.hasText(redisProperties.getPassword())) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        
        // 设置数据库
        singleServerConfig.setDatabase(redisProperties.getDatabase());
        
        // 设置连接超时
        if (redisProperties.getTimeout() != null) {
            singleServerConfig.setTimeout((int) redisProperties.getTimeout().toMillis());
        }
        
        return config;
    }

    /**
     * 应用自定义配置
     *
     * @param config Redisson配置
     * @param redissonProperties 自定义属性
     */
    private void applyCustomConfig(Config config, BubbleRedisProperties.Redisson redissonProperties) {
        // 设置线程池大小
        if (redissonProperties.getThreads() != null) {
            config.setThreads(redissonProperties.getThreads());
        }
        
        if (redissonProperties.getNettyThreads() != null) {
            config.setNettyThreads(redissonProperties.getNettyThreads());
        }
        
        // 获取单服务器配置
        SingleServerConfig singleServerConfig = getSingleServerConfig(config);
        if (singleServerConfig != null) {
            applySingleServerConfig(singleServerConfig, redissonProperties);
        }
    }

    /**
     * 获取单服务器配置（通过反射访问 protected 方法）
     *
     * @param config Redisson配置
     * @return SingleServerConfig
     */
    private SingleServerConfig getSingleServerConfig(Config config) {
        try {
            Method method = Config.class.getDeclaredMethod("getSingleServerConfig");
            method.setAccessible(true); // 允许访问 protected 方法
            return (SingleServerConfig) method.invoke(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access getSingleServerConfig via reflection", e);
        }
    }

    /**
     * 应用单服务器配置
     *
     * @param singleServerConfig 单服务器配置
     * @param redissonProperties 自定义属性
     */
    private void applySingleServerConfig(SingleServerConfig singleServerConfig, 
                                       BubbleRedisProperties.Redisson redissonProperties) {
        if (redissonProperties.getConnectionPoolSize() != null) {
            singleServerConfig.setConnectionPoolSize(redissonProperties.getConnectionPoolSize());
        }
        
        if (redissonProperties.getConnectionMinimumIdleSize() != null) {
            singleServerConfig.setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());
        }
        
        if (redissonProperties.getIdleConnectionTimeout() != null) {
            singleServerConfig.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout());
        }
        
        if (redissonProperties.getConnectTimeout() != null) {
            singleServerConfig.setConnectTimeout(redissonProperties.getConnectTimeout());
        }
        
        if (redissonProperties.getTimeout() != null) {
            singleServerConfig.setTimeout(redissonProperties.getTimeout());
        }
        
        if (redissonProperties.getRetryAttempts() != null) {
            singleServerConfig.setRetryAttempts(redissonProperties.getRetryAttempts());
        }
        
        if (redissonProperties.getRetryInterval() != null) {
            singleServerConfig.setRetryInterval(redissonProperties.getRetryInterval());
        }
        
        if (redissonProperties.getSubscriptionsPerConnection() != null) {
            singleServerConfig.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection());
        }
        
        if (StringUtils.hasText(redissonProperties.getClientName())) {
            singleServerConfig.setClientName(redissonProperties.getClientName());
        }
    }
}