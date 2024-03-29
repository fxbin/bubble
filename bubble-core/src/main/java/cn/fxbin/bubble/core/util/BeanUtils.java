package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.exception.UtilException;
import cn.fxbin.bubble.core.util.time.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BeanUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 13:51
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@UtilityClass
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * initialInstance
     *
     * @since 2020/5/7 17:28
     * @param clazz the class to instantiate
     * @return T 泛型
     * @see org.springframework.beans.BeanUtils#instantiateClass(java.lang.Class)
     */
    public <T> T initialInstance(Class<?> clazz) {
        return (T) instantiateClass(clazz);
    }

    /**
     * initialInstance
     *
     * @since 2020/3/30 14:18
     * @param className class name
     * @return T
     */
    public <T> T initialInstance(String className) {
        try {
            Class clazz = Class.forName(className);
            return initialInstance(clazz);
        } catch (ClassNotFoundException e) {
            throw new UtilException(e);
        }
    }


    /**
     * object2Map
     *
     * @since 2020/3/30 14:42
     * @param object java.lang.Object
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String, Object> object2Map(Object object) {
        return object2Map(object, false);
    }

    /**
     * object2Map
     *
     * @since 2020/5/28 10:36
     * @param object java.lang.Object
     * @param timestampDefault 日期类型是否默认默认转时间戳
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String, Object> object2Map(Object object, boolean timestampDefault) {
        Assert.notNull(object, "object can't be null");
        BeanMap beanMap = BeanMap.create(object);
        // 时间戳的处理
        if (timestampDefault) {
            Map<String, Object> map = new HashMap<>();
            beanMap.keySet().forEach(key ->
                    map.put(String.valueOf(key), DateUtils.isDateType(beanMap.get(key))? DateUtils.toEpochMilli(beanMap.get(key)) : beanMap.get(key)));
            return map;
        } else {
            Map<String, Object> map = new HashMap<>();
            beanMap.keySet().forEach(key ->
                    map.put(String.valueOf(key),
                            DateUtils.isDateType(beanMap.get(key))? DateUtils.format(DateUtils.toLocalDateTime(DateUtils.toEpochMilli(beanMap.get(key))), DateUtils.NORM_DATETIME_PATTERN) : beanMap.get(key)));
            return map;
        }
    }


    /**
     * object2Map
     *
     * @since 2022/8/8 17:18
     * @param objectList java.lang.Object list
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public <T> List<Map<String, Object>> object2Map(List<T> objectList) {
        return object2Map(objectList, false);
    }

    /**
     * object2Map
     *
     * @since 2020/5/8 17:01
     * @param objectList  java.lang.Object list
     * @param timestampDefault 日期类型是否默认默认转时间戳
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public <T> List<Map<String, Object>> object2Map(List<T> objectList, boolean timestampDefault) {
        List list = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(objectList)) {
            list = objectList.stream()
                    .map(obj -> object2Map(obj, timestampDefault))
                    .collect(Collectors.toList());
        }
        return list;
    }


    /**
     * map2Object
     *
     * @since 2020/5/8 11:26
     * @param map java.util.Map
     * @param clazz the target object class
     * @return T 泛型
     */
    public <T> T map2Object(Map<?, ?> map, Class<T> clazz) {
        Assert.notNull(map, "map can't be null");
        Assert.notNull(clazz, "class can't be null");
        T target = initialInstance(clazz);
        BeanMap beanMap = BeanMap.create(target);
        beanMap.putAll(map);
        return target;
    }


    /**
     * copy
     *
     * @since 2020/5/7 18:22
     * @param source source object
     * @param target target object
     */
    public void copy(Object source, Object target) {
        BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), false);
        copier.copy(source, target, null);
    }


    /**
     * getProperty 获取Bean的属性
     *
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
     * @since 2020/3/30 15:20
     * @param bean bean
     * @param propertyName 属性名
     * @param value 属性值
     */
    public void setProperty(Object bean, String propertyName, Object value) {
        Objects.requireNonNull(bean, "bean Could not null");
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        beanWrapper.setPropertyValue(propertyName, value);
    }


}
