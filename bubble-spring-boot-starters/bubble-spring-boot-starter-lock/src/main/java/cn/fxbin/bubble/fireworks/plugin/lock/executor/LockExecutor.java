package cn.fxbin.bubble.fireworks.plugin.lock.executor;

import cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo;

/**
 * LockExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 17:28
 */
public interface LockExecutor {

    /**
     * tryLock
     *
     * @since 2020/8/4 17:34
     * @return boolean
     */
    boolean tryLock();

    /**
     * releaseLock
     *
     * @since 2020/8/4 17:34
     * @param lockInfo cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo
     * @return boolean
     */
    boolean releaseLock(LockInfo lockInfo);

}
