package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

/**
 * ObjectUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 16:54
 */
@UtilityClass
public class ObjectUtils extends org.springframework.util.ObjectUtils {

    /**
     * isNotEmpty
     *
     * @since 2020/4/9 18:29
     * @param obj the object to check
     * @return boolean
     * @see org.springframework.util.ObjectUtils#isEmpty(java.lang.Object)
     */
    public boolean isNotEmpty(@Nullable Object obj) {
        return !isEmpty(obj);
    }

    /**
     * isNotEmpty
     *
     * @since 2020/4/9 18:31
     * @param array the array to check
     * @return boolean
     * @see org.springframework.util.ObjectUtils#isEmpty(java.lang.Object[])
     */
    public static boolean isNotEmpty(@Nullable Object[] array) {
        return !isEmpty(array);
    }
}
