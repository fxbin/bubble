package cn.fxbin.bubble.plugin.lock.executor.redis;

import cn.fxbin.bubble.plugin.lock.executor.AbstractLockExecutor;
import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * MultiLockExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 18:24
 */
@Slf4j
public class MultiLockExecutor extends AbstractLockExecutor implements LockExecutor {

    private RedissonMultiLock rLock;

    private final RedissonClient redissonClient;

    private final LockInfo lockInfo;

    public MultiLockExecutor(RedissonClient redissonClient, LockInfo lockInfo) {
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
            RLock[] lockList = new RLock[lockInfo.getLockKey().size()];
            for (int i = 0; i < lockInfo.getLockKey().size(); i++) {
                lockList[i] = redissonClient.getLock(lockInfo.getLockKey().get(i));
            }
            rLock = new RedissonMultiLock(lockList);
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
     * @param lockInfo cn.fxbin.bubble.plugin.lock.model.LockInfo
     * @return boolean
     * @since 2020/8/4 17:34
     */
    @Override
    public boolean releaseLock(LockInfo lockInfo) {
        return isHeldByCurrentThread(lockInfo, rLock);
    }

}
