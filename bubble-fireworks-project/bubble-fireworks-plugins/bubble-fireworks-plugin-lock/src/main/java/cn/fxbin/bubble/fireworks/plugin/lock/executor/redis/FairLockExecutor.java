package cn.fxbin.bubble.fireworks.plugin.lock.executor.redis;

import cn.fxbin.bubble.fireworks.plugin.lock.executor.AbstractLockExecutor;
import cn.fxbin.bubble.fireworks.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * FairLockExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 17:50
 */
@Slf4j
public class FairLockExecutor extends AbstractLockExecutor implements LockExecutor {

    private RLock rLock;

    private final RedissonClient redissonClient;

    private final LockInfo lockInfo;

    public FairLockExecutor(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    /**
     * tryLock
     *
     * @return boolean
     * @since 2020/8/4 17:34
     */
    @Override
    public boolean tryLock() {
        try {
            rLock = redissonClient.getFairLock(getLockKey(lockInfo.getLockKey()));
            return rLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), lockInfo.getTimeUnit());
        } catch (InterruptedException e) {
            log.error("加锁失败信息，{}", lockInfo);
            log.error("加锁失败", e);
            return false;
        }
    }

    /**
     * releaseLock
     *
     * @param lockInfo cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo
     * @return boolean
     * @since 2020/8/4 17:34
     */
    @Override
    public boolean releaseLock(LockInfo lockInfo) {
        return isHeldByCurrentThread(lockInfo, rLock);
    }
}
