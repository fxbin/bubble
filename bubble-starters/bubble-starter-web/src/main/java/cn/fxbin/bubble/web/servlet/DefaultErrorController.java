package cn.fxbin.bubble.web.servlet;

import cn.fxbin.bubble.core.dataobject.Result;
import cn.fxbin.bubble.core.util.BeanUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * DefaultErrorController
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/8/16 13:06
 */
@RestController
public class DefaultErrorController extends BasicErrorController {

    public static ErrorProperties initProperties(){
        ErrorProperties properties = new ErrorProperties();
        properties.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        return properties;
    }

    public DefaultErrorController() {
        super(new DefaultErrorAttributes(), initProperties());
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }

        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        String path = (String) body.getOrDefault("path", "");
        String error = (String) body.get("error");
        String errmsg = String.format("path %s %s", path, error);

        Result<Object> bodyResult = Result.failure(errmsg);
        bodyResult.setTimestamp(System.currentTimeMillis());
        String traceId = resolveTraceId(request);
        if (traceId != null && !traceId.isEmpty()) {
            bodyResult.setTraceId(traceId);
        }
        return new ResponseEntity<>(BeanUtils.object2Map(bodyResult), status);
    }

    private String resolveTraceId(HttpServletRequest request) {
        try {
            Class<?> tracerUtils = Class.forName("cn.fxbin.bubble.plugin.logging.util.TracerUtils");
            Object traceId = tracerUtils.getMethod("getTraceId").invoke(null);
            if (traceId instanceof String s && s != null && !s.isEmpty() && !"N/A".equals(s)) {
                return s;
            }
        } catch (Throwable ignored) {}

        String mdcId = org.slf4j.MDC.get("traceId");
        if (mdcId != null && !mdcId.isEmpty()) {
            return mdcId;
        }

        String headerId = request.getHeader("X-Trace-Id");
        if (headerId != null && !headerId.isEmpty()) {
            return headerId;
        }
        return null;
    }

}
