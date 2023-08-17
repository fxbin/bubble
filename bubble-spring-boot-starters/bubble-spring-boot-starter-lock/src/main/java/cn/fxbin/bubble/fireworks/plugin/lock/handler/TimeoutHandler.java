package cn.fxbin.bubble.fireworks.plugin.lock.handler;

import cn.fxbin.bubble.fireworks.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo;

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
     * @param lockInfo cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo
     * @param lockExecutor cn.fxbin.bubble.fireworks.plugin.lock.executor.LockExecutor
     */
    void handle(LockInfo lockInfo, LockExecutor lockExecutor);

}
