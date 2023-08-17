package cn.fxbin.bubble.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * DefaultGlobalAsyncExceptionHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/29 16:45
 */
@SuppressWarnings("ALL")
@Slf4j
@Component
public class DefaultGlobalAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {


    /**
     * Handle the given uncaught exception thrown from an asynchronous method.
     *
     * @param ex     the exception thrown from the asynchronous method
     * @param method the asynchronous method
     * @param params the parameters used to invoked the method
     */
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("[handleUncaughtException][method({}) params({}) 发生异常]",
                method, params, ex);
    }

}
