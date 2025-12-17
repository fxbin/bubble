package cn.fxbin.bubble.flow.core.state.cache;

import java.util.Optional;

/**
 * Interface for managing the state of a flow context in a cache.
 *
 * @author fxbin
 * @since 2025/4/22
 */
public interface FlowStateCache {

    /**
     * 将序列化后的状态放入缓存。
     *
     * @param key 缓存键，通常由 flowId 和 executionId 生成。
     * @param serializedState 要存储的序列化状态字符串。
     */
    void put(String key, String serializedState);

    /**
     * 从缓存中检索序列化后的状态。
     *
     * @param key 缓存键。
     * @return 包含序列化状态的 Optional，如果未找到则返回空 Optional。
     */
    Optional<String> get(String key);

    /**
     * 将序列化后的状态放入缓存，并指定生存时间 (TTL)。
     *
     * @param key 缓存键。
     * @param serializedState 要存储的序列化状态字符串。
     * @param ttlSeconds 缓存条目的生存时间（秒）。
     */
    void put(String key, String serializedState, long ttlSeconds);

    /**
     * 从缓存中移除一个状态。
     *
     * @param key 缓存键。
     */
    void evict(String key);

    /**
     * Generates a cache key based on flow ID and execution ID.
     * This can be a default method if the pattern is common, or left to implementations.
     *
     * @param flowId The ID of the flow.
     * @param executionId The ID of the execution.
     * @return The generated cache key.
     */
    String generateCacheKey(Object flowId, String executionId);

}