package cn.fxbin.bubble.data.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 操作工具类
 * 提供常用的分布式数据结构操作
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/1/1 00:00
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonOperations {

    private final RedissonClient redissonClient;

    // ==================== 信号量和倒计时闩 ====================

    /**
     * 获取信号量
     *
     * @param name 信号量名称
     * @return RSemaphore
     */
    public RSemaphore getSemaphore(String name) {
        return redissonClient.getSemaphore(name);
    }

    /**
     * 获取倒计时闩
     *
     * @param name 倒计时闩名称
     * @return RCountDownLatch
     */
    public RCountDownLatch getCountDownLatch(String name) {
        return redissonClient.getCountDownLatch(name);
    }

    // ========== 数据结构操作 ==========

    /**
     * 获取分布式Map
     *
     * @param mapKey map的key
     * @param <K>    键类型
     * @param <V>    值类型
     * @return RMap
     */
    public <K, V> RMap<K, V> getMap(String mapKey) {
        return redissonClient.getMap(mapKey);
    }

    /**
     * 获取分布式Set
     *
     * @param setKey set的key
     * @param <V>    值类型
     * @return RSet
     */
    public <V> RSet<V> getSet(String setKey) {
        return redissonClient.getSet(setKey);
    }

    /**
     * 获取分布式List
     *
     * @param listKey list的key
     * @param <V>     值类型
     * @return RList
     */
    public <V> RList<V> getList(String listKey) {
        return redissonClient.getList(listKey);
    }

    /**
     * 获取分布式Queue
     *
     * @param queueKey queue的key
     * @param <V>      值类型
     * @return RQueue
     */
    public <V> RQueue<V> getQueue(String queueKey) {
        return redissonClient.getQueue(queueKey);
    }

    /**
     * 获取分布式Deque
     *
     * @param dequeKey deque的key
     * @param <V>      值类型
     * @return RDeque
     */
    public <V> RDeque<V> getDeque(String dequeKey) {
        return redissonClient.getDeque(dequeKey);
    }

    /**
     * 获取分布式阻塞队列
     *
     * @param queueKey queue的key
     * @param <V>      值类型
     * @return RBlockingQueue
     */
    public <V> RBlockingQueue<V> getBlockingQueue(String queueKey) {
        return redissonClient.getBlockingQueue(queueKey);
    }

    /**
     * 获取分布式延迟队列
     *
     * @param queueKey queue的key
     * @param <V>      值类型
     * @return RDelayedQueue
     */
    public <V> RDelayedQueue<V> getDelayedQueue(String queueKey) {
        RBlockingQueue<V> blockingQueue = getBlockingQueue(queueKey);
        return redissonClient.getDelayedQueue(blockingQueue);
    }

    /**
     * 获取分布式优先级队列
     *
     * @param queueKey queue的key
     * @param <V>      值类型
     * @return RPriorityQueue
     */
    public <V> RPriorityQueue<V> getPriorityQueue(String queueKey) {
        return redissonClient.getPriorityQueue(queueKey);
    }

    // ========== 原子操作 ==========

    /**
     * 获取原子长整型
     *
     * @param atomicKey 原子变量key
     * @return RAtomicLong
     */
    public RAtomicLong getAtomicLong(String atomicKey) {
        return redissonClient.getAtomicLong(atomicKey);
    }

    /**
     * 获取原子双精度浮点型
     *
     * @param atomicKey 原子变量key
     * @return RAtomicDouble
     */
    public RAtomicDouble getAtomicDouble(String atomicKey) {
        return redissonClient.getAtomicDouble(atomicKey);
    }

    // ========== 发布订阅 ==========

    /**
     * 获取主题
     *
     * @param topicKey 主题key
     * @return RTopic
     */
    public RTopic getTopic(String topicKey) {
        return redissonClient.getTopic(topicKey);
    }

    /**
     * 获取模式主题
     *
     * @param pattern 模式
     * @return RPatternTopic
     */
    public RPatternTopic getPatternTopic(String pattern) {
        return redissonClient.getPatternTopic(pattern);
    }

    // ========== 布隆过滤器 ==========

    /**
     * 获取布隆过滤器
     *
     * @param filterKey 过滤器key
     * @param <T>       元素类型
     * @return RBloomFilter
     */
    public <T> RBloomFilter<T> getBloomFilter(String filterKey) {
        return redissonClient.getBloomFilter(filterKey);
    }

    // ========== 限流器 ==========

    /**
     * 获取限流器
     *
     * @param rateLimiterKey 限流器key
     * @return RRateLimiter
     */
    public RRateLimiter getRateLimiter(String rateLimiterKey) {
        return redissonClient.getRateLimiter(rateLimiterKey);
    }

    // ========== 便捷方法 ==========



    /**
     * 设置Map中的值
     *
     * @param mapKey map的key
     * @param key    键
     * @param value  值
     * @param <K>    键类型
     * @param <V>    值类型
     */
    public <K, V> void mapPut(String mapKey, K key, V value) {
        RMap<K, V> map = getMap(mapKey);
        map.put(key, value);
    }

    /**
     * 获取Map中的值
     *
     * @param mapKey map的key
     * @param key    键
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 值
     */
    @Nullable
    public <K, V> V mapGet(String mapKey, K key) {
        RMap<K, V> map = getMap(mapKey);
        return map.get(key);
    }

    /**
     * 删除Map中的值
     *
     * @param mapKey map的key
     * @param key    键
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 被删除的值
     */
    @Nullable
    public <K, V> V mapRemove(String mapKey, K key) {
        RMap<K, V> map = getMap(mapKey);
        return map.remove(key);
    }

    /**
     * 向Set中添加元素
     *
     * @param setKey set的key
     * @param value  值
     * @param <V>    值类型
     * @return 是否添加成功
     */
    public <V> boolean setAdd(String setKey, V value) {
        RSet<V> set = getSet(setKey);
        return set.add(value);
    }

    /**
     * 从Set中移除元素
     *
     * @param setKey set的key
     * @param value  值
     * @param <V>    值类型
     * @return 是否移除成功
     */
    public <V> boolean setRemove(String setKey, V value) {
        RSet<V> set = getSet(setKey);
        return set.remove(value);
    }

    /**
     * 检查Set中是否包含元素
     *
     * @param setKey set的key
     * @param value  值
     * @param <V>    值类型
     * @return 是否包含
     */
    public <V> boolean setContains(String setKey, V value) {
        RSet<V> set = getSet(setKey);
        return set.contains(value);
    }

    /**
     * 向List末尾添加元素
     *
     * @param listKey list的key
     * @param value   值
     * @param <V>     值类型
     * @return 是否添加成功
     */
    public <V> boolean listAdd(String listKey, V value) {
        RList<V> list = getList(listKey);
        return list.add(value);
    }

    /**
     * 从List中获取指定索引的元素
     *
     * @param listKey list的key
     * @param index   索引
     * @param <V>     值类型
     * @return 元素值
     */
    @Nullable
    public <V> V listGet(String listKey, int index) {
        RList<V> list = getList(listKey);
        return list.get(index);
    }

    /**
     * 获取List的大小
     *
     * @param listKey list的key
     * @return 大小
     */
    public int listSize(String listKey) {
        RList<?> list = getList(listKey);
        return list.size();
    }

    /**
     * 发布消息到主题
     *
     * @param topicKey 主题key
     * @param message  消息
     * @return 接收到消息的客户端数量
     */
    public long publish(String topicKey, Object message) {
        RTopic topic = getTopic(topicKey);
        return topic.publish(message);
    }

    /**
     * 订阅主题
     *
     * @param topicKey 主题key
     * @param listener 监听器
     * @param <M>      消息类型
     * @return 监听器ID
     */
    public <M> int subscribe(String topicKey, MessageListener<M> listener) {
        RTopic topic = getTopic(topicKey);
        return topic.addListener(Object.class, listener);
    }

    /**
     * 取消订阅
     *
     * @param topicKey   主题key
     * @param listenerId 监听器ID
     */
    public void unsubscribe(String topicKey, int listenerId) {
        RTopic topic = getTopic(topicKey);
        topic.removeListener(listenerId);
    }

    /**
     * 删除key
     *
     * @param key 要删除的key
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 检查key是否存在
     *
     * @param key 要检查的key
     * @return 是否存在
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 设置key的过期时间
     *
     * @param key      key
     * @param duration 过期时间
     * @return 是否设置成功
     */
    public boolean expire(String key, Duration duration) {
        return redissonClient.getBucket(key).expire(duration);
    }

    /**
     * 获取RedissonClient实例
     *
     * @return RedissonClient
     */
    public RedissonClient getRedissonClient() {
        return redissonClient;
    }
}