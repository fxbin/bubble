package cn.fxbin.bubble.plugin.lock.model;

import cn.fxbin.bubble.plugin.lock.exception.LockTimeoutException;
import cn.fxbin.bubble.core.util.ThreadUtils;
import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.handler.TimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * LockTimeoutStrategy
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 15:05
 */
@Slf4j
public enum LockTimeoutStrategy implements TimeoutHandler {

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
            log.warn("try lock failed, do nothing");
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
            throw new LockTimeoutException("try lock failed, execute [FAIL_FAST Strategy], lock key is {} ", lockInfo.getLockKey());
        }
    },

    RETRY() {

        private final AtomicInteger count = new AtomicInteger(0);

        /**
         * handle
         *
         * @param lockInfo     cn.fxbin.bubble.plugin.lock.model.LockInfo
         * @param lockExecutor cn.fxbin.bubble.plugin.lock.executor.LockExecutor
         * @since 2020/8/5 15:03
         */
        @Override
        public void handle(LockInfo lockInfo, LockExecutor lockExecutor) {
            while (count.get() <= lockInfo.getRetry()) {
                if (lockExecutor.tryLock()) {
                    ThreadUtils.sleep(100);
                    count.incrementAndGet();
                }
            }
            throw new LockTimeoutException("try lock retry {} times failed, lock key is {} with timeout {} {}", count.get(), lockInfo.getLockKey(), lockInfo.getWaitTime(), lockInfo.getTimeUnit().name());
        }
    },

    BLOCK() {
        /**
         * handle
         *
         * @param lockInfo     cn.fxbin.bubble.plugin.lock.model.LockInfo
         * @param lockExecutor cn.fxbin.bubble.plugin.lock.executor.LockExecutor
         * @since 2020/8/5 15:03
         */
        @Override
        public void handle(LockInfo lockInfo, LockExecutor lockExecutor) {

            long defaultInterval = lockInfo.getWaitTime();
            final long defaultMaxInterval = lockInfo.getWaitTime() * Math.max(3, Runtime.getRuntime().availableProcessors());

            while (!lockExecutor.tryLock()) {

                if (defaultInterval > defaultMaxInterval) {
                    throw new LockTimeoutException("try lock failed, lock key {} use too many times, this may dead lock occurs.", lockInfo.getLockKey());
                }

                try {
                    lockInfo.getTimeUnit().sleep(defaultInterval);
                    defaultInterval <<= 1;
                } catch (InterruptedException e) {
                    throw new LockTimeoutException("try lock failed, lock key is {} with timeout {} {}", lockInfo.getLockKey(), lockInfo.getWaitTime(), lockInfo.getTimeUnit().name());
                }
            }
        }
    };

}
