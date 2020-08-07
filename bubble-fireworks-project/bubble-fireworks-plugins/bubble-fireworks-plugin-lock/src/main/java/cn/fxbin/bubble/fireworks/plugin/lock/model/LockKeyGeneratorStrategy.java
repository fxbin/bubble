package cn.fxbin.bubble.fireworks.plugin.lock.model;

/**
 * LockKeyGeneratorStrategy
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/7 17:13
 */
public enum LockKeyGeneratorStrategy {

    /**
     * 报名+方法名+自定义Key
     */
    Sample,

    /**
     * Spring EL 表达式
     */
    Expression;

}
