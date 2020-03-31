package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.exception.UtilException;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * BeanUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 13:51
 */
@UtilityClass
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * initialInstance
     *
     * @author fxbin
     * @since 2020/3/30 14:18
     * @param className class name
     * @return T
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> T initialInstance(String className) {
        try {
            Class clazz = Class.forName(className);
            return (T) ClassUtils.initialInstance(clazz);
        } catch (ClassNotFoundException e) {
            throw new UtilException(e);
        }
    }


    /**
     * object2Map
     *
     * @author fxbin
     * @since 2020/3/30 14:42
     * @param object java.lang.Object
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> object2Map(Object object) {
        Assert.notNull(object, "object can't be null");
        return BeanMap.create(object);
    }


    /**
     * getProperty 获取Bean的属性
     *
     * @author fxbin
     * @since 2020/3/30 15:19
     * @param bean bean
     * @param propertyName 属性名
     * @return java.lang.Object
     */
    @Nullable
    public Object getProperty(@Nullable Object bean, String propertyName) {
        if (bean == null) {
            return null;
        }
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        return beanWrapper.getPropertyValue(propertyName);
    }


    /**
     * setProperty 设置Bean属性
     *
     * @author fxbin
     * @since 2020/3/30 15:20
     * @param bean bean
     * @param propertyName 属性名
     * @param value 属性值
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        Objects.requireNonNull(bean, "bean Could not null");
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        beanWrapper.setPropertyValue(propertyName, value);
    }


}
