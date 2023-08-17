package cn.fxbin.bubble.fireworks.core.util.support;

import java.io.Serializable;
import java.util.function.Function;

/**
 * SerialFunction
 *
 * <p>
 *     支持序列化的 Function
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/9/24 13:51
 */
@FunctionalInterface
public interface SerialFunction<T, R> extends Function<T, R>, Serializable {
}
