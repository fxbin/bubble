package cn.fxbin.bubble.plugin.lock.handler;

import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.model.LockInfo;

/**
 * LockTimeoutHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 14:56
 */
@FunctionalInterface
public interface TimeoutHandler {

    /**
     * handle
     *
     * @since 2020/8/5 15:03
     * @param lockInfo cn.fxbin.bubble.plugin.lock.model.LockInfo
     * @param lockExecutor cn.fxbin.bubble.plugin.lock.executor.LockExecutor
     */
    void handle(LockInfo lockInfo, LockExecutor lockExecutor);

}
