package cn.fxbin.bubble.plugin.lock.model;

import cn.fxbin.bubble.plugin.lock.annotation.LockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * LockInfo
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/13 16:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockInfo implements Serializable {
    private static final long serialVersionUID = 8393432794041178448L;

    /**
     * 锁名称
     */
    @NonNull
    private List<String> lockKey;

    /**
     * 锁值
     */
    @NonNull
    private String lockValue;

    /**
     * 锁类型
     */
    private LockType lockType = LockType.Default;

    /**
     * 失效时间
     */
    private Long leaseTime = 30 * 1000L;

    /**
     * 超时时间
     */
    private Long waitTime = 3000L;

    /**
     * 获取加锁重试次数
     */
    private Long retry;

    /**
     * 时间单位
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /**
     * 加锁超时处理策略
     */
    private LockTimeoutStrategy lockTimeoutStrategy;

    /**
     * 释放锁超时处理策略
     */
    private ReleaseTimeoutStrategy releaseTimeoutStrategy;

}
