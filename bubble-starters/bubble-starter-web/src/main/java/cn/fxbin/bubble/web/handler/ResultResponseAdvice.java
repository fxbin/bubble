package cn.fxbin.bubble.web.handler;

import cn.fxbin.bubble.core.dataobject.Result;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResultResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?> r) {
            if (r.getTimestamp() == null) {
                r.setTimestamp(System.currentTimeMillis());
            }
            if (r.getTraceId() == null) {
                String traceId = resolveTraceId(request);
                if (traceId != null && !traceId.isEmpty()) {
                    r.setTraceId(traceId);
                }
            }
            return r;
        }
        return body;
    }

    private String resolveTraceId(ServerHttpRequest request) {
        try {
            Class<?> tracerUtils = Class.forName("cn.fxbin.bubble.plugin.logging.util.TracerUtils");
            Object traceId = tracerUtils.getMethod("getTraceId").invoke(null);
            if (traceId instanceof String s && s != null && !s.isEmpty() && !"N/A".equals(s)) {
                return s;
            }
        } catch (Throwable ignored) {}

        String mdcId = MDC.get("traceId");
        if (mdcId != null && !mdcId.isEmpty()) {
            return mdcId;
        }

        String headerId = request.getHeaders().getFirst("X-Trace-Id");
        if (headerId != null && !headerId.isEmpty()) {
            return headerId;
        }

        return null;
    }
}
