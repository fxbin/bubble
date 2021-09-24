package cn.fxbin.bubble.fireworks.plugin.logging.aspect;

import cn.fxbin.bubble.fireworks.core.util.*;
import cn.fxbin.bubble.fireworks.core.util.ttl.TtlMap;
import cn.fxbin.bubble.fireworks.plugin.logging.LoggingFactoryBean;
import cn.fxbin.bubble.fireworks.plugin.logging.event.LoggingNoticeEvent;
import cn.fxbin.bubble.fireworks.plugin.logging.model.BubbleFireworksLogging;
import cn.hutool.core.date.SystemClock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * LoggingWebAspect
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 17:28
 */
@Slf4j
@Aspect
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LoggingWebAspect extends AbstractLogging {

    @Resource
    private LoggingFactoryBean factoryBean;

    @Resource
    private ApplicationEventPublisher publisher;

    @Pointcut(value = "(@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.web.bind.annotation.RestController))"
    )
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        return logGenerate(point);
    }

    private Object logGenerate(ProceedingJoinPoint point) throws Throwable {
        HttpServletResponse response = WebUtils.getResponse();
        long startNs = SystemClock.now();

        // 初始化日志对象实例
        BubbleFireworksLogging fireworksLogging = initializeLog();

        // 请求参数
        Object[] args = point.getArgs();
        String requestBody = JsonUtils.isJsonSerialize(args) ? JsonUtils.toJson(args) : StringUtils.utf8Str(args);

        Object responseObj = null;
        try {

            log.info("RequestUri: 「{}」, IP: 「{}」, Method:「{}」, Param:「{}」",
                    fireworksLogging.getRequestUri(), fireworksLogging.getRequestIp(), fireworksLogging.getRequestMethod(), requestBody);

            responseObj = point.proceed();
            return responseObj;
        } catch (Exception e) {
            log.warn("exception : " + e.getMessage(), e);
            fireworksLogging.setExceptionStack(ExceptionUtils.getStackTrace(e));
            throw e;
        } finally {
            long endNs = SystemClock.now();
            long tookMs = TimeUnit.MILLISECONDS.toMillis(endNs - startNs);

            fireworksLogging
                    // 请求体
                    .setRequestBody(requestBody)
                    // 响应内容记录
                    .setResponseStatus(response.getStatus())
                    .setResponseBody(JsonUtils.isJsonSerialize(responseObj) ? JsonUtils.toJson(responseObj) : StringUtils.utf8Str(responseObj))
                    .setResponseHeaders(WebUtils.getResponseHeaders(response))
                    // 请求耗时记录
                    .setStartRequestTime(startNs)
                    .setEndResponseTime(endNs)
                    .setTimeConsuming(tookMs);

            // ttl 清理器
            ttlClear();

            log.info("ResponseBody: 「{}」, ResponseStatus: 「{}」, Consuming: 「{}ms」",
                    fireworksLogging.getResponseBody(), fireworksLogging.getResponseStatus(), fireworksLogging.getTimeConsuming());

            publisher.publishEvent(new LoggingNoticeEvent(fireworksLogging));
        }
    }


    /**
     * initializeLog
     *
     * @since 2020/5/19 17:50
     * @return cn.fxbin.bubble.fireworks.plugin.logging.model.BubbleFireworksLogging
     */
    private BubbleFireworksLogging initializeLog() {

        HttpServletRequest request = WebUtils.getRequest();

        return BubbleFireworksLogging.builder()
                .serviceId(factoryBean.getServiceId())
                .servicePort(factoryBean.getServicePort())
                .serviceIp(factoryBean.getServiceIp())
                // spanid & traceid
                .spanId(getSpanId())
                .traceId(getTraceId())
                // 请求信息
                .requestIp(WebUtils.getIpAddr(request))
                .requestUri(request.getRequestURI())
                .requestMethod(request.getMethod())
                .requestHeaders(WebUtils.getRequestHeaders(request))
                .build();
    }

    private void ttlClear() {
        TtlMap.clear();
    }

}
