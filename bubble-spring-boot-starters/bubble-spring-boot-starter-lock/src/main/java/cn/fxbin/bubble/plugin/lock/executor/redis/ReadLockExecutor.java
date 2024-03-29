package cn.fxbin.bubble.plugin.lock.executor.redis;

import cn.fxbin.bubble.plugin.lock.executor.AbstractLockExecutor;
import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ExecutionException;

/**
 * ReadWriteLockExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 17:52
 */
@Slf4j
public class ReadLockExecutor extends AbstractLockExecutor implements LockExecutor {

    private RReadWriteLock rLock;

    private final RedissonClient redissonClient;

    private final LockInfo lockInfo;

    public ReadLockExecutor(RedissonClient redissonClient, LockInfo lockInfo) {
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
            rLock = redissonClient.getReadWriteLock(getLockKey(lockInfo.getLockKey()));
            return rLock.readLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), lockInfo.getTimeUnit());
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
        if(rLock.readLock().isHeldByCurrentThread()){
            try {
                return rLock.readLock().forceUnlockAsync().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("释放锁失败信息，{}", lockInfo);
                log.error("释放锁失败", e);
                return false;
            }
        }
        return false;
    }
}
