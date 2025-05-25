package cn.fxbin.bubble.plugin.lock.executor.redis;

import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.plugin.lock.executor.AbstractLockExecutor;
import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

/**
 * DefaultExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 10:32
 */
@Slf4j
public class DefaultRedisExecutor extends AbstractLockExecutor implements LockExecutor {

    /**
     * 解决Redis分布式锁setnx后setexpire因某种问题导致没执行，导致锁一直被占的问题；
     */
    private static final RedisScript<String> SCRIPT_LOCK = new DefaultRedisScript<>(
            "return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])", String.class);


    private static final RedisScript<String> SCRIPT_UNLOCK = new DefaultRedisScript<>(
            "if redis.call('get',KEYS[1]) == ARGV[1] then return tostring(redis.call('del', KEYS[1])==1) else return 'false' end", String.class);

    private static final String LOCK_SUCCESS = "OK";

    private final RedisTemplate redisTemplate;

    private final LockInfo lockInfo;


    public DefaultRedisExecutor(RedisTemplate redisTemplate, LockInfo lockInfo) {
        this.redisTemplate = redisTemplate;
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
        String locked = (String) redisTemplate.execute(SCRIPT_LOCK,
                redisTemplate.getStringSerializer(), redisTemplate.getStringSerializer(),
                Collections.singletonList(getLockKey(lockInfo.getLockKey())), lockInfo.getLockValue(), StringUtils.utf8Str(lockInfo.getLeaseTime()));
        return LOCK_SUCCESS.equalsIgnoreCase(locked);
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
        Object execute = redisTemplate.execute(SCRIPT_UNLOCK,
                redisTemplate.getStringSerializer(),
                redisTemplate.getStringSerializer(),
                Collections.singletonList(getLockKey(lockInfo.getLockKey())),
                lockInfo.getLockValue());

        assert execute != null;
        return Boolean.parseBoolean(execute.toString());
    }
}
