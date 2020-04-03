package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ThreadLocalUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:18
 */
@SuppressWarnings("unchecked")
@UtilityClass
public class ThreadLocalUtils {

    private static final ThreadLocal<Map<String, Object>> LOCAL_CACHE = ThreadLocal.withInitial(HashMap::new);

    /**
     * getAll threadLocal中的全部值
     *
     * @since 2020/3/20 18:19
     * @return java.util.Map<java.lang.String,java.lang.Object>
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
        localCache().put(key, value);
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
