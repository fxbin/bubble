package cn.fxbin.bubble.plugin.dynamic.threadpool.support;

import cn.fxbin.bubble.core.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ThreadPoolRejectedRecordOperations
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/10 15:39
 */
public class ThreadPoolRejectedRecordOperations {

    /**
     * save thread pool reject task count,
     * Key: thread pool name, Value: thread pool reject task count
     */
    private final Map<String, AtomicLong> threadRejectCountMap = new HashMap<>();

    /**
     * put
     *
     * @since 2020/7/10 15:44
     * @param poolName thread pool name
     */
    public void put(String poolName) {
        AtomicLong rejectedCount = threadRejectCountMap.putIfAbsent(poolName, new AtomicLong(1));
        if (ObjectUtils.isNotEmpty(rejectedCount)) {
            Objects.requireNonNull(rejectedCount).getAndIncrement();
        }
    }


    /**
     * get
     *
     * according to thread name get the rejected task number
     *
     * @since 2020/7/10 15:44
     * @param poolName thread pool name
     * @return java.lang.Long
     */
    public Long get(String poolName) {
        if (ObjectUtils.isEmpty(threadRejectCountMap.get(poolName))) {
            return 0L;
        }
        return threadRejectCountMap.get(poolName).get();
    }


    /**
     * clear
     *
     * @since 2020/7/10 15:46
     */
    public void clear() {
        threadRejectCountMap.clear();
    }

}
