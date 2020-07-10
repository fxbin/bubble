package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.support;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ThreadExecutorsOperations
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/10 11:00
 */
public class ThreadPoolExecutorOperations {

    /**
     * save thread pool object,
     * Key:thread pool name, Value:ThreadPoolExecutor object
     */
    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap = new HashMap<>(16);

    /**
     * put
     *
     * @since 2020/7/10 11:09
     * @param poolName thread pool name
     * @param executor java.util.concurrent.ThreadPoolExecutor
     */
    public void put(String poolName, ThreadPoolExecutor executor) {
        if (this.containsKey(poolName)) {
            return;
        }
        threadPoolExecutorMap.put(poolName, executor);
    }

    /**
     * containsKey
     *
     * @since 2020/7/10 11:09
     * @param poolName thread pool name
     * @return <tt>true</tt> if this map contains a mapping for the specified key
     */
    public boolean containsKey(String poolName) {
        return threadPoolExecutorMap.containsKey(poolName);
    }

    /**
     * get
     *
     * @since 2020/7/10 11:10
     * @param poolName thread pool name
     * @return java.util.concurrent.ThreadPoolExecutor
     */
    public ThreadPoolExecutor get(String poolName) {
        return threadPoolExecutorMap.get(poolName);
    }


    /**
     * keys
     *
     * @since 2020/7/10 11:10
     * @return java.util.Set<java.lang.String>
     */
    public Set<String> keys() {
        return threadPoolExecutorMap.keySet();
    }

    /**
     * values
     *
     * @since 2020/7/10 11:10
     * @return java.util.Collection<java.util.concurrent.ThreadPoolExecutor>
     */
    public Collection<ThreadPoolExecutor> values() {
        return threadPoolExecutorMap.values();
    }

    /**
     * clear
     *
     * @since 2020/7/10 15:46
     */
    public void clear () {
        threadPoolExecutorMap.clear();
    }

}
