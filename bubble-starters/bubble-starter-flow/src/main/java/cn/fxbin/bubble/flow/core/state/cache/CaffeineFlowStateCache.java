package cn.fxbin.bubble.flow.core.state.cache;

import cn.fxbin.bubble.core.util.StringUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-based implementation of {@link cn.fxbin.bubble.flow.core.state.cache.FlowStateCache}.
 *
 * @author fxbin
 * @since 2025/4/22
 */
@Slf4j
public class CaffeineFlowStateCache implements FlowStateCache {

    private static final String CAFFEINE_CACHE_PREFIX = "flowctx:caffeine:";
    private final Cache<String, String> stateCache;

    /**
     * 构造函数，初始化 Caffeine 缓存。
     *
     * @param expireAfterWriteHours 写入后过期时间（小时）。
     * @param maximumSize 缓存最大容量。
     */
    public CaffeineFlowStateCache(long expireAfterWriteHours, long maximumSize) {
        this.stateCache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWriteHours, TimeUnit.HOURS)
                .maximumSize(maximumSize)
                .build();
        log.info("CaffeineFlowStateCache initialized with expireAfterWriteHours: {} and maximumSize: {}", expireAfterWriteHours, maximumSize);
    }

    /**
     * 将序列化后的状态放入缓存。
     *
     * @param key 缓存键。
     * @param serializedState 序列化后的状态字符串。
     */
    @Override
    public void put(String key, String serializedState) {
        stateCache.put(key, serializedState);
        log.debug("State saved to Caffeine with key: {}", key);
    }

    @Override
    public Optional<String> get(String key) {
        String state = stateCache.getIfPresent(key);
        if (state != null) {
            log.debug("State loaded from Caffeine with key: {}", key);
        }
        return Optional.ofNullable(state);
    }

    @Override
    public void put(String key, String serializedState, long ttlSeconds) {
        // Caffeine's per-entry expiry is more complex to set up after build.
        // For simplicity, this implementation relies on the global expiry policy.
        // If per-entry TTL is strictly needed, a different cache setup or a wrapper around Cache might be required.
        log.warn("Per-entry TTL is not directly supported by this CaffeineFlowStateCache implementation's current setup. Using global expiry for key: {}.", key);
        // Uses the global expiry
        put(key, serializedState);
    }

    @Override
    public void evict(String key) {
        stateCache.invalidate(key);
        log.debug("State evicted from Caffeine with key: {}", key);
    }

    @Override
    public String generateCacheKey(Object flowId, String executionId) {
        return StringUtils.format("{}{}:{}", CAFFEINE_CACHE_PREFIX, flowId, executionId);
    }
}