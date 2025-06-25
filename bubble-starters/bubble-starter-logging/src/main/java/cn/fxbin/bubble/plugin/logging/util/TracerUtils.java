/*
 * Copyright (c) 2020-2030, Shuigedeng (981376577@qq.com & https://blog.taotaocloud.top/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.fxbin.bubble.plugin.logging.util;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * SOFATracer 分布式链路追踪工具类
 * <p>
 * 提供获取当前链路追踪信息的便捷方法，包括TraceId和SpanId的获取
 * 支持SOFATracer框架的分布式链路追踪功能
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/12/19 15:30
 */
@Slf4j
@UtilityClass
public class TracerUtils {

    /**
     * 默认的TraceId值，当无法获取到真实TraceId时使用
     */
    private static final String DEFAULT_TRACE_ID = "N/A";

    /**
     * 默认的SpanId值，当无法获取到真实SpanId时使用
     */
    private static final String DEFAULT_SPAN_ID = "N/A";

    /**
     * 获取当前请求的TraceId
     * <p>
     * TraceId是分布式链路追踪中用于标识一次完整请求链路的唯一标识符
     * 在整个请求链路中保持不变，用于关联所有相关的日志和监控数据
     * </p>
     *
     * @return 当前请求的TraceId，如果无法获取则返回默认值
     */
    public static String getTraceId() {
        try {
            SofaTraceContext traceContext = SofaTraceContextHolder.getSofaTraceContext();
            if (traceContext != null) {
                SofaTracerSpan currentSpan = traceContext.getCurrentSpan();
                if (currentSpan != null && currentSpan.getSofaTracerSpanContext() != null) {
                    return currentSpan.getSofaTracerSpanContext().getTraceId();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get TraceId from SOFATracer: {}", e.getMessage());
        }
        return DEFAULT_TRACE_ID;
    }

    /**
     * 获取当前请求的SpanId
     * <p>
     * SpanId是分布式链路追踪中用于标识当前操作节点的唯一标识符
     * 在同一个TraceId下，不同的操作会有不同的SpanId
     * </p>
     *
     * @return 当前请求的SpanId，如果无法获取则返回默认值
     */
    public static String getSpanId() {
        try {
            SofaTraceContext traceContext = SofaTraceContextHolder.getSofaTraceContext();
            if (traceContext != null) {
                SofaTracerSpan currentSpan = traceContext.getCurrentSpan();
                if (currentSpan != null && currentSpan.getSofaTracerSpanContext() != null) {
                    return currentSpan.getSofaTracerSpanContext().getSpanId();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get SpanId from SOFATracer: {}", e.getMessage());
        }
        return DEFAULT_SPAN_ID;
    }

    /**
     * 获取当前链路追踪的完整信息
     * <p>
     * 返回格式化的链路追踪信息字符串，包含TraceId和SpanId
     * 格式：[TraceId:xxx,SpanId:xxx]
     * </p>
     *
     * @return 格式化的链路追踪信息字符串
     */
    public static String getTraceInfo() {
        String traceId = getTraceId();
        String spanId = getSpanId();
        return String.format("[TraceId:%s,SpanId:%s]", traceId, spanId);
    }

    /**
     * 检查当前是否存在有效的链路追踪上下文
     * <p>
     * 用于判断当前环境是否已经启用了SOFATracer链路追踪功能
     * </p>
     *
     * @return 如果存在有效的链路追踪上下文则返回true，否则返回false
     */
    public static boolean hasActiveTrace() {
        try {
            SofaTraceContext traceContext = SofaTraceContextHolder.getSofaTraceContext();
            if (traceContext != null) {
                SofaTracerSpan currentSpan = traceContext.getCurrentSpan();
                return currentSpan != null && currentSpan.getSofaTracerSpanContext() != null;
            }
        } catch (Exception e) {
            log.debug("Failed to check active trace: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取当前Span的操作名称
     * <p>
     * 操作名称通常用于标识当前正在执行的具体操作类型
     * </p>
     *
     * @return 当前Span的操作名称，如果无法获取则返回空字符串
     */
    public static String getOperationName() {
        try {
            SofaTraceContext traceContext = SofaTraceContextHolder.getSofaTraceContext();
            if (traceContext != null) {
                SofaTracerSpan currentSpan = traceContext.getCurrentSpan();
                if (currentSpan != null) {
                    return currentSpan.getOperationName();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get operation name: {}", e.getMessage());
        }
        return "";
    }
}