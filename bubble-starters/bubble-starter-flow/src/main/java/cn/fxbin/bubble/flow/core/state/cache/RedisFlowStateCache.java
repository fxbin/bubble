package cn.fxbin.bubble.flow.core.state.cache;

import cn.fxbin.bubble.core.util.ApplicationContextHolder;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.data.redis.RedisOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Optional;

/**
 * Redis-based implementation of {@link FlowStateCache}.
 *
 * @author fxbin
 * @since 2025/4/22
 */
@Slf4j
public class RedisFlowStateCache implements FlowStateCache {

    private static final String REDIS_CACHE_PREFIX = "flowctx:redis:";
    private final RedisOperations redisOperations;
    private final long defaultExpireSeconds;

    /**
     * 构造函数，初始化 Redis 操作对象和默认过期时间。
     *
     * @param defaultExpireHours 默认过期时间（小时）。
     */
    public RedisFlowStateCache(long defaultExpireHours) {
        this.redisOperations = initializeRedisOperations();
        this.defaultExpireSeconds = defaultExpireHours * 3600;
        log.info("RedisFlowStateCache initialized with defaultExpireHours: {}", defaultExpireHours);
    }

    /**
     * 初始化 Redis 操作对象。
     *
     * @return Redis 操作对象。
     * @throws IllegalStateException 如果 Redis 操作对象未找到。
     */
    private RedisOperations initializeRedisOperations() {
        try {
            return ApplicationContextHolder.getBean(RedisOperations.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("RedisOperations bean not found. Redis L2 cache will be unavailable.");
            // Depending on the application's requirements, you might throw an exception
            // or return a dummy/no-op implementation if Redis is optional.
            throw new IllegalStateException("RedisOperations bean not found, RedisFlowStateCache cannot operate.", e);
        }
    }

    @Override
    public void put(String key, String serializedState) {
        try {
            redisOperations.set(key, serializedState, defaultExpireSeconds);
            log.debug("State saved to Redis with key: {} and default TTL: {} seconds", key, defaultExpireSeconds);
        } catch (Exception e) {
            log.error("Failed to save state to Redis with key: {}. Error: {}", key, e.getMessage(), e);
            // Handle Redis unavailability or errors as per application's fault tolerance strategy
        }
    }

    @Override
    public Optional<String> get(String key) {
        try {
            Object state = redisOperations.get(key);
            if (state != null) {
                log.debug("State loaded from Redis with key: {}", key);
                return Optional.of(String.valueOf(state));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to load state from Redis with key: {}. Error: {}", key, e.getMessage(), e);
            // Or rethrow, depending on error handling strategy
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String serializedState, long ttlSeconds) {
        try {
            redisOperations.set(key, serializedState, ttlSeconds);
            log.debug("State saved to Redis with key: {} and TTL: {} seconds", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Failed to save state to Redis with key: {} and TTL: {}. Error: {}", key, ttlSeconds, e.getMessage(), e);
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisOperations.delete(key);
            log.debug("State evicted from Redis with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict state from Redis with key: {}. Error: {}", key, e.getMessage(), e);
        }
    }

    @Override
    public String generateCacheKey(Object flowId, String executionId) {
        return StringUtils.format("{}{}:{}", REDIS_CACHE_PREFIX, flowId, executionId);
    }
}