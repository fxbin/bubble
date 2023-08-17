package cn.fxbin.bubble.plugin.lock.factory;

import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.executor.redis.*;
import cn.fxbin.bubble.plugin.lock.model.LockInfo;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * LockFactory
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 16:41
 */
@Component
public class LockFactory {

    @Resource(name = "redisson")
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate redisTemplate;

    public LockExecutor get(LockInfo lockInfo) {
        switch (lockInfo.getLockType()) {
            case ReentrantLock:
                return new ReentrantLockExecutor(redissonClient, lockInfo);
            case FairLock:
                return new FairLockExecutor(redissonClient, lockInfo);
            case ReadLock:
                return new ReadLockExecutor(redissonClient, lockInfo);
            case WriteLock:
                return new WriteLockExecutor(redissonClient, lockInfo);
            case MultiLock:
                return new MultiLockExecutor(redissonClient, lockInfo);
            case RedLock:
                return new RedLockExecutor(redissonClient, lockInfo);
            default:
                return new DefaultRedisExecutor(redisTemplate, lockInfo);
        }
    }
}
