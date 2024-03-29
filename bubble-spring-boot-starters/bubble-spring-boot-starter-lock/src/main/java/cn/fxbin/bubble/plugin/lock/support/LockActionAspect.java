package cn.fxbin.bubble.plugin.lock.support;

import cn.fxbin.bubble.core.constant.StringPool;
import cn.fxbin.bubble.core.util.AnnotationUtils;
import cn.fxbin.bubble.core.util.ArrayUtils;
import cn.fxbin.bubble.core.util.RunTimeUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.plugin.lock.annotation.LockAction;
import cn.fxbin.bubble.plugin.lock.executor.LockExecutor;
import cn.fxbin.bubble.plugin.lock.factory.LockFactory;
import cn.fxbin.bubble.plugin.lock.model.LockInfo;
import cn.fxbin.bubble.plugin.lock.model.LockKeyGeneratorStrategy;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;

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


    @Pointcut("@annotation(cn.fxbin.bubble.plugin.lock.annotation.LockAction)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        LockInfo lockInfo = null;
        LockExecutor lockExecutor = null;

        try {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            LockAction lockAction = methodSignature.getMethod().getAnnotation(LockAction.class);

            String lockName = getKey(methodSignature.getMethod(), lockAction, point);
            String lockValue = RunTimeUtils.getPid() + ":" + StringUtils.getUUID();

            lockInfo = LockInfo.builder()
                    .lockKey(Lists.newArrayList(ArrayUtils.isEmpty(lockAction.keys()) ? new String[]{lockName} : lockAction.keys().length > 1 ? lockAction.keys() : new String[]{lockName}))
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

    private String getKey(Method method, LockAction lockAction, ProceedingJoinPoint point) {

        if(ArrayUtils.isEmpty(lockAction.keys()) || StringPool.EMPTY.equals(lockAction.keys()[0])) {
            AnnotationUtils.setValue(lockAction, "keyGeneratorType", LockKeyGeneratorStrategy.Sample);
        }

        switch (lockAction.keyGeneratorType()) {
            case Expression:
                return KEY_GENERATOR.generate(lockAction.keys()[0], method, point.getArgs());
            case Sample:
            default:
                return (String) KEY_GENERATOR.generate(method, lockAction);
        }
    }

}
