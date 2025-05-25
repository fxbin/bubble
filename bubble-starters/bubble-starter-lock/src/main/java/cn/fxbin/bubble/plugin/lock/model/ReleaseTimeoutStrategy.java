package cn.fxbin.bubble.plugin.lock.model;

import cn.fxbin.bubble.plugin.lock.exception.LockTimeoutException;
import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.handler.TimeoutHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ReleaseTimeoutStrategy
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 15:35
 */
@Slf4j
public enum ReleaseTimeoutStrategy implements TimeoutHandler {

    /**
     * 不做任何处理，继续执行业务
     */
    NO_OPERATION() {
        /**
         * handle
         *
         * @param lockInfo     cn.fxbin.bubble.plugin.lock.model.LockInfo
         * @param lockExecutor cn.fxbin.bubble.plugin.lock.executor.LockExecutor
         * @since 2020/8/5 15:03
         */
        @Override
        public void handle(LockInfo lockInfo, LockExecutor lockExecutor) {
            log.info("already been released while lock lease time, do nothing");
        }
    },


    FAIL_FAST() {
        /**
         * handle
         *
         * @param lockInfo     cn.fxbin.bubble.plugin.lock.model.LockInfo
         * @param lockExecutor cn.fxbin.bubble.plugin.lock.executor.LockExecutor
         * @since 2020/8/5 15:03
         */
        @Override
        public void handle(LockInfo lockInfo, LockExecutor lockExecutor) {
            throw new LockTimeoutException("already been released while lock lease time , lock key is {} with lease time {} {}", lockInfo.getLockKey(), lockInfo.getLeaseTime(), lockInfo.getTimeUnit().name());
        }
    };
}
