package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.exception.UtilException;
import cn.fxbin.bubble.fireworks.core.logging.LoggerMessageFormat;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * AnnotationUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/24 15:48
 */
@Slf4j
@UtilityClass
public class AnnotationUtils extends org.springframework.core.annotation.AnnotationUtils {

    /**
     * setValue
     *
     * @since 2020/7/24 16:24
     * @param annotation 注解
     * @param attributeName 属性名
     * @param attributeValue 属性值
     */
    public void setValue(Annotation annotation, String attributeName, Object attributeValue) {
        Assert.notNull(annotation, "annotation is not allowed null");
        Assert.notNull(attributeName, "attributeName is not allowed null");
        Assert.notNull(attributeValue, "attributeValue is not allowed null");

        try {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);

            Field memberValuesField = invocationHandler.getClass().getDeclaredField("memberValues");
            memberValuesField.setAccessible(true);
            Map memberValues = (Map) memberValuesField.get(invocationHandler);
            if (memberValues.containsKey(attributeName)) {
                memberValues.put(attributeName, attributeValue);
            } else {
                log.warn("{} 注解属性 {} 修改失败, {}", annotation.getClass(), attributeName, annotation);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UtilException(LoggerMessageFormat.format("{} {} error",
                    AnnotationUtils.class, Thread.currentThread().getStackTrace()[1].getMethodName()), e);
        }
    }

}
