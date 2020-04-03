package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.exception.UtilException;
import cn.fxbin.bubble.fireworks.core.logging.LoggerMessageFormat;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * ArrayUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 17:34
 */
@Slf4j
@UtilityClass
public class ArrayUtils {

    /**
     * isEmpty
     *
     * <p>
     *  数组是否为空<br>
     *  此方法会匹配单一对象，如果此对象为{@code null}则返回true<br>
     *  如果此对象为非数组，理解为此对象为数组的第一个元素，则返回false<br>
     *  如果此对象为数组对象，数组长度大于0情况下返回false，否则返回true
     * </p>
     *
     * @since 2020/3/23 17:53
     * @param array 数组
     * @return boolean
     */
    public static boolean isEmpty(Object array) {
        if (null == array) {
            return true;
        } else if (isArray(array)) {
            return 0 == Array.getLength(array);
        }
        throw new UtilException("Object to provide is not a Array !");
    }

    /**
     * isArray 对象是否为数组对象
     *
     * @since 2020/3/23 17:52
     * @param obj 对象
     * @return 是否为数组对象，如果为{@code null} 返回false
     */
    public static boolean isArray(Object obj) {
        if (null == obj) {
            // throw new NullPointerException("Object check for isArray is null");
            return false;
        }
        return obj.getClass().isArray();
    }

    /**
     * wrap 包装数组对象
     *
     * @since 2020/3/23 17:55
     * @param obj 对象，可以是对象数组或者基本类型数组
     * @return java.lang.Object[]
     * @throws UtilException 对象为非数组
     */
    public static Object[] wrap(Object obj) {
        if (null == obj) {
            return null;
        }
        if (isArray(obj)) {
            try {
                return (Object[]) obj;
            } catch (Exception e) {
                final String className = obj.getClass().getComponentType().getName();
                switch (className) {
                    case "long":
                        return wrap((long[]) obj);
                    case "int":
                        return wrap((int[]) obj);
                    case "short":
                        return wrap((short[]) obj);
                    case "char":
                        return wrap((char[]) obj);
                    case "byte":
                        return wrap((byte[]) obj);
                    case "boolean":
                        return wrap((boolean[]) obj);
                    case "float":
                        return wrap((float[]) obj);
                    case "double":
                        return wrap((double[]) obj);
                    default:
                        throw new UtilException(e);
                }
            }
        }
        throw new UtilException((LoggerMessageFormat.format("[{}] is not Array!", obj.getClass())));
    }

    /**
     * 数组或集合转String
     *
     * @param obj 集合或数组对象
     * @return 数组字符串，与集合转字符串格式相同
     */
    public static String toString(Object obj) {
        if (null == obj) {
            return null;
        }
        if (ArrayUtils.isArray(obj)) {
            try {
                return Arrays.deepToString((Object[]) obj);
            } catch (Exception e) {
                log.error("cn.fxbin.bubble.core.util.ArrayUtils.toString error", e);
                return Arrays.toString(wrap(obj));
            }
        }
        return obj.toString();
    }

}
