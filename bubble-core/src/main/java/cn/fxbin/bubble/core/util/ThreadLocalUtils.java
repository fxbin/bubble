package cn.fxbin.bubble.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ThreadLocalUtils - 优化版本
 * 提供线程安全的本地缓存工具，支持内存泄漏防护
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:18
 */
@Slf4j
@SuppressWarnings("unchecked")
@UtilityClass
public class ThreadLocalUtils {

    private final ThreadLocal<Map<String, Object>> LOCAL_CACHE = ThreadLocal.withInitial(HashMap::new);
    
    // 最大缓存大小，防止内存泄漏
    private final int MAX_CACHE_SIZE = 100;

    /**
     * getAll threadLocal中的全部值
     *
     * @since 2020/3/20 18:19
     * @return {@link java.util.Map<java.lang.String,java.lang.Object>}
     */
    public Map<String, Object> getAll() {
        return localCache();
    }


    /**
     * 设置一个值到ThreadLocal
     *
     * @param key   键
     * @param value 值
     * @param <T>   值的类型
     * @return 被放入的值
     * @see Map#put(Object, Object)
     */
    public <T> T put(String key, T value) {
        if (key == null) {
            log.warn("ThreadLocal key cannot be null");
            return value;
        }
        
        Map<String, Object> cache = localCache();
        
        // 检查缓存大小，防止内存泄漏
        if (cache.size() >= MAX_CACHE_SIZE && !cache.containsKey(key)) {
            log.warn("ThreadLocal cache size exceeded maximum limit: {}, clearing cache", MAX_CACHE_SIZE);
            cache.clear();
        }
        
        cache.put(key, value);
        return value;
    }


    /**
     * remove 删除参数对应的值
     *
     * @since 2020/3/20 18:27
     * @param key 键
     * @see Map#remove(java.lang.Object)
     */
    public void remove(String key) {
        localCache().remove(key);
    }


    /**
     * clear
     *
     * @since 2020/3/20 18:28
     * @see Map#clear()
     */
    public void clear() {
        localCache().clear();
    }


    /**
     * remove
     *
     * @since 2020/3/20 18:30
     * @see ThreadLocal#remove()
     */
    public void remove(){
        LOCAL_CACHE.remove();
    }


    /**
     * get
     *
     * @since 2020/3/20 18:26
     * @param key 键
     * @param <T> 值泛型
     * @return 值, 不存在则返回null, 如果类型与泛型不一致, 可能抛出{@link ClassCastException}
     * @see Map#get(Object)
     * @see ClassCastException
     */
    public <T> T get(String key) {
        return (T) localCache().get(key);
    }


    /**
     * getIfAbsent 从ThreadLocal中获取值,并指定一个当值不存在的提供者
     *
     * @since 2020/3/20 18:24
     * @param key 键
     * @param supplierOnNull java.util.function.Supplier
     * @return T
     * @see Supplier
     */
    public <T> T getIfAbsent(String key, Supplier<T> supplierOnNull) {
        return (T) localCache().computeIfAbsent(key, k -> supplierOnNull.get());
    }


    /**
     * getAndRemove 获取一个值后然后删除掉
     *
     * @since 2020/3/20 18:24
     * @param key 键
     * @param <T> 值类型
     * @return 值, 不存在则返回null
     * @see #get(String)
     * @see #remove(String)
     */
    public <T> T getAndRemove(String key) {
        try {
            return get(key);
        } finally {
            remove(key);
        }
    }


    /**
     * localCache
     *
     * @since 2020/3/20 18:24
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> localCache(){
        Map<String, Object> map = LOCAL_CACHE.get();
        if(map == null){
            map = new HashMap<>();
            LOCAL_CACHE.set(map);
        }
        return map;
    }

}
