package cn.fxbin.bubble.core.util;

import lombok.experimental.UtilityClass;

import static org.springframework.beans.BeanUtils.instantiateClass;

/**
 * ClassUtil
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 17:24
 */
@UtilityClass
public class ClassUtils extends org.springframework.util.ClassUtils {

    /**
     * initialInstance 实例化对象
     *
     * @since 2020/3/30 14:10
     * @param clazz java.lang.Class
     * @return T
     */
    public <T> T initialInstance(Class<T> clazz) {
        return instantiateClass(clazz);
    }

}
