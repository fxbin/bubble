package cn.fxbin.bubble.fireworks.plugin.lock.aop.support;

import cn.fxbin.bubble.fireworks.core.util.CollectionUtils;
import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import cn.fxbin.bubble.fireworks.core.util.RunTimeUtils;
import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction;
import cn.fxbin.bubble.fireworks.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.fireworks.plugin.lock.factory.LockFactory;
import cn.fxbin.bubble.fireworks.plugin.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * LockInterceptor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/13 19:38
 */
@Slf4j
@Aspect
@Order(-1)
public class LockActionAspect {

    private static final LockKeyGenerator KEY_GENERATOR = new LockKeyGenerator();

    @Resource
    private LockFactory lockFactory;


    @Pointcut("@annotation(cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        LockInfo lockInfo = null;
        LockExecutor lockExecutor = null;

        try {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            LockAction lockAction = methodSignature.getMethod().getAnnotation(LockAction.class);
            String lockName = (String) KEY_GENERATOR.generate(methodSignature.getMethod(), lockAction);
            String lockValue = RunTimeUtils.getPid() + ":" + StringUtils.getUUID();

            lockInfo = LockInfo.builder()
                    .lockKey(CollectionUtils.arrayToList(ObjectUtils.isEmpty(lockAction.keys()) ? new String[]{lockName} : lockAction.keys().length > 1 ? lockAction.keys() : new String[]{lockName}))
                    .lockValue(lockValue)
                    .lockType(lockAction.lockType())
                    .leaseTime(lockAction.leaseTime())
                    .waitTime(lockAction.waitTime())
                    .timeUnit(lockAction.timeUnit())
                    .retry(lockAction.retry())
                    .lockTimeoutStrategy(lockAction.lockTimeoutStrategy())
                    .releaseTimeoutStrategy(lockAction.releaseTimeoutStrategy())
                    .build();
            lockExecutor = lockFactory.get(lockInfo);
            boolean locked = lockExecutor.tryLock();
            // 如果加锁失败，执行降级处理策略
            if (!locked) {
                lockInfo.getLockTimeoutStrategy().handle(lockInfo, lockExecutor);
            }
            log.info("{} 加锁成功", lockInfo.getLockKey());
            return point.proceed();
        } finally {
            Assert.notNull(lockInfo, "lockinfo is not allowed null");
            Assert.notNull(lockExecutor, "lockExecutor is not allowed null");
            if (!lockExecutor.releaseLock(lockInfo)) {
                lockInfo.getReleaseTimeoutStrategy().handle(lockInfo, lockExecutor);
            }
        }
    }

}
