package cn.fxbin.bubble.fireworks.plugin.logging.aspect;

import org.slf4j.MDC;

/**
 * AbstractLogging
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 16:18
 */
public abstract class AbstractLogging {

    public String getTraceId() {
        return MDC.get("traceId");
    }

    public String getSpanId() {
        return MDC.get("spanId");
    }

}
