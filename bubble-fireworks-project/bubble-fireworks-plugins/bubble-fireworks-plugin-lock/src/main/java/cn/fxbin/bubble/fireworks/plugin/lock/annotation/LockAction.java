package cn.fxbin.bubble.fireworks.plugin.lock.annotation;

import cn.fxbin.bubble.fireworks.plugin.lock.model.LockTimeoutStrategy;
import cn.fxbin.bubble.fireworks.plugin.lock.model.ReleaseTimeoutStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Lock
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/13 16:51
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LockAction {

    /**
     * keys
     */
    String[] keys() default {""};

    /**
     * 进程号 + uuid
     */
    String value() default "";

    /**
     * 锁类型
     */
    LockType lockType() default LockType.Default;

    /**
     * 过期时间(必须是大于业务代码执行时间,默认30秒) 单位: 毫秒
     */
    long leaseTime() default 30 * 1000;

    /**
     * 获取锁超时时间（默认3000毫秒） 单位: 毫秒
     */
    long waitTime() default 3000;

    /**
     * 时间单位，默认：毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 获取锁重试次数，默认3次
     */
    long retry() default 3;

    /**
     * 加锁超时策略
     */
    LockTimeoutStrategy lockTimeoutStrategy() default LockTimeoutStrategy.NO_OPERATION;

    /**
     * 释放锁超时策略
     */
    ReleaseTimeoutStrategy releaseTimeoutStrategy() default ReleaseTimeoutStrategy.NO_OPERATION;

}
