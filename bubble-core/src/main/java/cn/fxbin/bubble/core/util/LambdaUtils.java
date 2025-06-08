package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.util.support.SerialFunction;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LambdaUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/9/24 15:34
 */
public class LambdaUtils {

    private static final Map<Class<?>, WeakReference<SerializedLambda>> CLASS_WEAK_REFERENCE_CACHE = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param <T>  Lambda类型
     * @param func 需要解析的 lambda 对象（无参方法）
     * @return 返回解析后的结果
     */
    public static <T> SerializedLambda resolve(SerialFunction<T, ?> func) {
        return CLASS_WEAK_REFERENCE_CACHE.computeIfAbsent(func.getClass(),
                        serial -> new WeakReference<>(ReflectUtil.invoke(func, "writeReplace")))
                .get();
    }

    /**
     * 获取lambda表达式函数（方法）名称
     *
     * @param <T>  Lambda类型
     * @param func 函数（无参方法）
     * @return 函数名称
     */
    public static <T> String getMethodName(SerialFunction<T, ?> func) {
        return resolve(func).getImplMethodName();
    }

    /**
     * loadClass
     *
     * @since 2021/9/29 09:52
     * @param func 函数（无参方法）
     * @return {@link java.lang.Class<?>}
     */
    public static <T> Class<?> loadClass(SerialFunction<T, ?> func) {
        return CLASS_CACHE.computeIfAbsent(func.getClass(),
                apply -> ClassUtil.loadClass(resolve(func).getImplClass().replaceAll("/", ".")));
    }

    /**
     * 获取lambda表达式Getter或Setter函数（方法）对应的字段名称，规则如下：
     * <ul>
     *     <li>getXxxx获取为xxxx，如getName得到name。</li>
     *     <li>setXxxx获取为xxxx，如setName得到name。</li>
     *     <li>isXxxx获取为xxxx，如isName得到name。</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param <T>  Lambda类型
     * @param func 函数（无参方法）
     * @return 函数名称
     * @throws IllegalArgumentException 非Getter或Setter方法
     */
    public static <T> String getFieldName(SerialFunction<T, ?> func) throws IllegalArgumentException {
        final String methodName = getMethodName(func);
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return StringUtils.removePreAndLowerFirst(methodName, 3);
        } else if (methodName.startsWith("is")) {
            return StringUtils.removePreAndLowerFirst(methodName, 2);
        } else {
            throw new IllegalArgumentException("Invalid Getter or Setter name: " + methodName);
        }
    }


}
