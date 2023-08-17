package cn.fxbin.bubble.fireworks.plugin.lock.executor;

import cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * AbstractLockExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 18:20
 */
@Slf4j
public abstract class AbstractLockExecutor {

    public String getLockKey(List<String> lockKeyList) {
        Assert.notEmpty(lockKeyList, "lock key list is not allowed empty");
        return lockKeyList.get(0);
    }

    public boolean isHeldByCurrentThread(LockInfo lockInfo, RLock rLock) {
        if(rLock.isHeldByCurrentThread()){
            try {
                return rLock.forceUnlockAsync().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("释放锁失败信息，{}", lockInfo);
                log.error("释放锁失败", e);
                return false;
            }
        }
        return false;
    }

}
