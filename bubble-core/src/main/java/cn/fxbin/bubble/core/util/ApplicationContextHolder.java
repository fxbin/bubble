package cn.fxbin.bubble.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * ApplicationContextHolder
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 17:37
 */
@Slf4j
@Component
@Lazy(false)
public class ApplicationContextHolder implements ApplicationContextAware, DisposableBean {

    @Nullable
    private static ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }


    /**
     * getBean 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     *
     * @since 2020/3/20 18:01
     * @param name bean name
     * @return T
     * @throws IllegalStateException 如果 ApplicationContext 尚未初始化
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertApplicationContextInitialized();
        return (T) applicationContext.getBean(name);
    }


    /**
     * getBean 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     *
     * @since 2020/3/20 17:56
     * @param requiredType type the bean must match; can be an interface or superclass
     * @return T
     * @throws IllegalStateException 如果 ApplicationContext 尚未初始化
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertApplicationContextInitialized();
        return applicationContext.getBean(requiredType);
    }

    /**
     * getBean 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     *
     * @since 2020/3/20 17:56
     * @param name bean name
     * @param requiredType type the bean must match; can be an interface or superclass
     * @return T
     * @throws IllegalStateException 如果 ApplicationContext 尚未初始化
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        assertApplicationContextInitialized();
        return applicationContext.getBean(name, requiredType);
    }

    /**
     * 检查 ApplicationContext 是否已初始化
     *
     * @throws IllegalStateException 如果 ApplicationContext 尚未初始化
     */
    private static void assertApplicationContextInitialized() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext 尚未初始化，请确保 SpringContextHolder 已被 Spring 容器管理");
        }
    }


    /**
     * publishEvent 发布事件
     *
     * @since 2020/3/20 18:03
     * @param event org.springframework.context.ApplicationEvent
     */
    public static void publishEvent(ApplicationEvent event) {
        if (applicationContext == null) {
            return;
        }
        applicationContext.publishEvent(event);
    }


    /**
     * clearProvider 清除SpringContextProvider中的ApplicationContext为Null.
     *
     * @since 2020/3/20 17:57
     */
    private static void clearProvider() {
        if(log.isDebugEnabled()) {
            log.debug("清除SpringContextProvider中的ApplicationContext为: {}", applicationContext);
        }
        applicationContext = null;
    }


    @Override
    public void destroy() throws Exception {
        clearProvider();
    }
}
