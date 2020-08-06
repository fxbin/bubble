package cn.fxbin.bubble.fireworks.plugin.lock.annotation;

/**
 * LockType
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 13:58
 */
public enum LockType {

    /**
     * 可重入锁
     */
    ReentrantLock,

    /**
     * 公平锁
     */
    FairLock,

    /**
     * 红锁
     */
    RedLock,

    /**
     * 联锁
     */
    MultiLock,

    /**
     * 读锁
     */
    ReadLock,

    /**
     * 写锁
     */
    WriteLock,

    /**
     * 默认
     */
    Default;


}
