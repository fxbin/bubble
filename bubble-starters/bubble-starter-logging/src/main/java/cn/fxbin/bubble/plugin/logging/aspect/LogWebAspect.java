package cn.fxbin.bubble.plugin.logging.aspect;

import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.core.util.WebUtils;
import cn.fxbin.bubble.plugin.logging.autoconfigure.LoggingProperties;
import cn.fxbin.bubble.plugin.logging.model.SysLogRecord;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * LogWebAspect
 *
 * <p>
 * Web层日志切面，专门用于记录Spring MVC控制器方法的执行日志。
 * 继承自AbstractLogAspect，复用通用的日志记录逻辑，并针对Web层进行特化处理。
 * </p>
 *
 * <p>
 * 主要功能：
 * - 拦截所有@Controller和@RestController注解的类中的方法
 * - 记录HTTP请求的详细信息（URL、方法、参数、头信息等）
 * - 记录HTTP响应的状态码和响应体
 * - 支持通过配置忽略特定的URL路径
 * - 集成分布式链路追踪，记录traceId和spanId
 * - 提供详细的异常信息记录
 * </p>
 *
 * <p>
 * 配置说明：
 * - 通过bubble.logging.web.enabled控制是否启用Web层日志
 * - 通过bubble.logging.web.ignore-urls配置需要忽略的URL列表
 * - 仅在Web应用环境下生效
 * </p>
 *
 * <p>
 * 性能考虑：
 * - 使用@Order注解控制切面执行顺序
 * - 异步处理日志输出，减少对业务方法的性能影响
 * - 智能参数处理，避免记录过大的对象
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/24 17:27
 */
@Slf4j
@Aspect
@Order(1)
@RequiredArgsConstructor
@ConditionalOnWebApplication
public class LogWebAspect extends AbstractLogAspect {

    /**
     * 日志配置属性
     * 用于获取Web层日志的相关配置，如忽略URL列表等
     */
    private final LoggingProperties loggingProperties;

    /**
     * 定义Web层切点
     * 
     * <p>
     * 拦截所有标注了@Controller或@RestController注解的类中的方法。
     * 这样可以确保所有的Web请求处理方法都会被日志记录。
     * </p>
     */
    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    @Override
    public void pointCut() {
        // 切点定义，实际逻辑在注解中
    }

    /**
     * Web层日志记录的环绕通知
     * 
     * <p>
     * 重写父类的logRecord方法，增加Web层特有的处理逻辑：
     * - 获取HTTP请求和响应信息
     * - 检查URL是否在忽略列表中
     * - 记录请求头、响应状态码等Web特有信息
     * - 处理Web层特有的异常情况
     * </p>
     * 
     * @param joinPoint 连接点，包含被拦截方法的信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("pointCut()")
    @Override
    public Object logRecord(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取HTTP请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 非Web环境，使用父类默认处理
            return super.logRecord(joinPoint);
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        
        // 检查是否需要忽略此请求
        if (shouldIgnoreRequest(request)) {
            return joinPoint.proceed();
        }

        // 记录开始时间
        long startNs = System.nanoTime();
        long startTime = System.currentTimeMillis();
        
        Object result = null;
        Throwable exception = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            try {
                // 记录结束时间
                long endNs = System.nanoTime();
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                
                // 构建Web层专用的日志记录
                SysLogRecord logRecord = buildWebLogRecord(joinPoint, request, response, 
                    startTime, endTime, startNs, endNs, costTime, result, exception);
                
                // 输出格式化的日志
                String formattedLog = formatLogRecord(logRecord);
                if (exception != null) {
                    log.error("Web request failed:\n{}", formattedLog);
                } else {
                    log.info("Web request completed:\n{}", formattedLog);
                }
            } catch (Exception e) {
                log.error("Error occurred while logging web request", e);
            }
        }
    }

    /**
     * 构建Web层专用的日志记录
     * 
     * <p>
     * 在父类基础日志记录的基础上，增加Web层特有的信息：
     * - HTTP请求方法、URL、查询参数
     * - 请求头信息（过滤敏感信息）
     * - 客户端IP地址
     * - HTTP响应状态码
     * - 用户代理信息
     * - 根据配置控制响应体记录
     * </p>
     * 
     * @param joinPoint 连接点信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param startTime 开始时间（毫秒）
     * @param endTime 结束时间（毫秒）
     * @param startNs 开始时间（纳秒）
     * @param endNs 结束时间（纳秒）
     * @param costTime 执行耗时（毫秒）
     * @param result 方法返回值
     * @param exception 异常信息
     * @return 构建完成的Web层日志记录对象
     */
    protected SysLogRecord buildWebLogRecord(ProceedingJoinPoint joinPoint, HttpServletRequest request, 
            HttpServletResponse response, long startTime, long endTime, long startNs, long endNs, 
            long costTime, Object result, Throwable exception) {
        
        // 处理响应体（根据配置决定是否记录）
        Object responseBody = null;
        if (shouldLogResponseBody()) {
            Object processedResult = processResult(result);
            if (processedResult != null) {
                String resultJson = JsonUtils.toJson(processedResult);
                responseBody = truncateContent(resultJson, getMaxResponseBodyLength());
            }
        }
        
        return SysLogRecord.builder()
                .serviceName(getServiceName())
                .traceId(getTraceId())
                .spanId(getSpanId())
                .eventName(getWebEventName(request, joinPoint))
                .requestBody(buildRequestInfo(request, joinPoint.getArgs()))
                .responseBody(responseBody)
                .requestHeaders(getFilteredHeaders(request))
                .responseHeaders(getResponseHeaders(response))
                .requestIp(getClientIpAddress(request))
                .status(response != null ? response.getStatus() : null)
                .costTime(costTime)
                .startNs(startNs)
                .endNs(endNs)
                .exceptionStack(exception != null ? getExceptionStack(exception) : null)
                .build();
    }

    /**
     * 生成Web层事件名称
     * 
     * <p>
     * 结合HTTP方法和请求路径生成更具描述性的事件名称。
     * 格式：HTTP_METHOD /request/path -> ClassName.methodName
     * </p>
     * 
     * @param request HTTP请求对象
     * @param joinPoint 连接点
     * @return Web层事件名称
     */
    protected String getWebEventName(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String methodName = getEventName(joinPoint);
        return String.format("%s %s -> %s", httpMethod, requestUri, methodName);
    }

    /**
     * 构建请求信息
     * 
     * <p>
     * 整合HTTP请求的各种信息，包括URL参数、请求体、方法参数等。
     * 提供完整的请求上下文信息用于日志记录和问题排查。
     * 根据配置控制是否记录请求体内容。
     * </p>
     * 
     * @param request HTTP请求对象
     * @param args 方法参数
     * @return 包含完整请求信息的对象
     */
    protected Object buildRequestInfo(HttpServletRequest request, Object[] args) {
        Map<String, Object> requestInfo = new HashMap<>();
        
        // 基本请求信息
        requestInfo.put("method", request.getMethod());
        requestInfo.put("uri", request.getRequestURI());
        requestInfo.put("url", request.getRequestURL().toString());
        
        // 查询参数
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            requestInfo.put("queryString", queryString);
        }
        
        // URL参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            parameterMap.forEach((key, values) -> {
                if (values.length == 1) {
                    params.put(key, values[0]);
                } else {
                    params.put(key, Arrays.asList(values));
                }
            });
            requestInfo.put("parameters", params);
        }
        
        // 请求体内容（根据配置决定是否记录）
        if (shouldLogRequestBody()) {
            String requestBody = getRequestBody(request);
            if (StringUtils.isNotBlank(requestBody)) {
                requestInfo.put("requestBody", truncateContent(requestBody, getMaxRequestBodyLength()));
            }
        }
        
        // 方法参数（过滤HTTP相关对象）
        Object processedArgs = processParameters(args);
        if (processedArgs != null) {
            requestInfo.put("methodArgs", processedArgs);
        }
        
        return requestInfo;
    }

    /**
     * 获取过滤后的请求头
     * 
     * <p>
     * 提取HTTP请求头信息，过滤掉敏感信息（如Authorization、Cookie等）。
     * 保留对调试和问题排查有用的头信息。
     * 使用配置中的敏感头列表进行动态过滤。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return 过滤后的请求头Map
     */
    protected Map<String, Object> getFilteredHeaders(HttpServletRequest request) {
        Map<String, Object> headers = new HashMap<>();
        
        // 从配置获取敏感头信息列表，转换为小写用于匹配
        Set<String> sensitiveHeaders = new HashSet<>();
        if (loggingProperties.getWeb() != null && 
            loggingProperties.getWeb().getSensitiveHeaders() != null) {
            loggingProperties.getWeb().getSensitiveHeaders().forEach(header -> 
                sensitiveHeaders.add(header.toLowerCase()));
        }
        
        // 添加默认的敏感头（如果配置为空）
        if (sensitiveHeaders.isEmpty()) {
            sensitiveHeaders.addAll(Set.of(
                "authorization", "cookie", "x-auth-token", "x-api-key", 
                "authentication", "proxy-authorization"
            ));
        }
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String lowerHeaderName = headerName.toLowerCase();
            
            if (sensitiveHeaders.contains(lowerHeaderName)) {
                headers.put(headerName, "[FILTERED]");
            } else {
                String headerValue = request.getHeader(headerName);
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }

    /**
     * 获取响应头信息
     * 
     * <p>
     * 提取HTTP响应头信息，主要包含Content-Type、Content-Length等
     * 对调试有用的响应头信息。
     * </p>
     * 
     * @param response HTTP响应对象
     * @return 响应头Map，如果响应对象为null则返回null
     */
    protected Map<String, Object> getResponseHeaders(HttpServletResponse response) {
        if (response == null) {
            return null;
        }
        
        Map<String, Object> headers = new HashMap<>();
        
        // 获取常用的响应头
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            headers.put(headerName, response.getHeader(headerName));
        }
        
        return headers.isEmpty() ? null : headers;
    }

    /**
     * 获取客户端IP地址
     * 
     * <p>
     * 智能获取客户端的真实IP地址，考虑代理、负载均衡等场景。
     * 按优先级检查各种可能包含真实IP的请求头。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    protected String getClientIpAddress(HttpServletRequest request) {
        try {
            return WebUtils.getIpAddr(request);
        } catch (Exception e) {
            log.debug("Failed to get client IP address", e);
            return request.getRemoteAddr();
        }
    }

    /**
     * 检查是否应该忽略当前请求
     * 
     * <p>
     * 根据配置的忽略URL列表，判断当前请求是否需要跳过日志记录。
     * 支持精确匹配和通配符匹配。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return true表示应该忽略，false表示需要记录日志
     */
    protected boolean shouldIgnoreRequest(HttpServletRequest request) {
        if (ignoreList.isEmpty()) {
            return false;
        }
        
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 移除上下文路径
        if (StringUtils.isNotBlank(contextPath) && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }
        
        // 检查是否匹配忽略列表
        for (String ignorePattern : ignoreList) {
            if (StrUtil.isBlank(ignorePattern)) {
                continue;
            }
            
            // 支持通配符匹配
            if (ignorePattern.contains("*")) {
                String regex = ignorePattern.replace("*", ".*");
                if (requestUri.matches(regex)) {
                    return true;
                }
            } else {
                // 精确匹配
                if (requestUri.equals(ignorePattern)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 检查是否应该记录请求体
     * 
     * @return true表示应该记录请求体
     */
    protected boolean shouldLogRequestBody() {
        return loggingProperties.getWeb() != null && 
               loggingProperties.getWeb().isLogRequestBody();
    }
    
    /**
     * 检查是否应该记录响应体
     * 
     * @return true表示应该记录响应体
     */
    protected boolean shouldLogResponseBody() {
        return loggingProperties.getWeb() != null && 
               loggingProperties.getWeb().isLogResponseBody();
    }
    
    /**
     * 获取请求体最大长度配置
     * 
     * @return 请求体最大长度
     */
    protected int getMaxRequestBodyLength() {
        return loggingProperties.getWeb() != null ? 
               loggingProperties.getWeb().getMaxRequestBodyLength() : 1000;
    }
    
    /**
     * 获取响应体最大长度配置
     * 
     * @return 响应体最大长度
     */
    protected int getMaxResponseBodyLength() {
        return loggingProperties.getWeb() != null ? 
               loggingProperties.getWeb().getMaxResponseBodyLength() : 1000;
    }
    
    /**
     * 获取请求体内容
     * 
     * <p>
     * 从HTTP请求中读取请求体内容。
     * 注意：由于InputStream只能读取一次，这里使用ServletUtil工具类来安全读取。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return 请求体内容字符串
     */
    protected String getRequestBody(HttpServletRequest request) {
        try {
            // 检查请求方法是否支持请求体
            String method = request.getMethod();
            if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method) && 
                !"PATCH".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
                return "";
            }
            
            // 检查Content-Type
            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
                return "[MULTIPART_DATA]";
            }
            
            // 使用hutool的ServletUtil读取请求体
            return WebUtils.getRequestBody(request);
        } catch (Exception e) {
            log.warn("获取请求体失败: {}", e.getMessage());
            return "[FAILED_TO_READ]";
        }
    }
    
    /**
     * 格式化Web层日志记录
     *
     * <p>
     * 重写父类方法，提供更适合Web请求的可读格式。
     * </p>
     *
     * @param logRecord 日志记录对象
     * @return 格式化后的日志字符串
     */
    @Override
    protected String formatLogRecord(SysLogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n======================================== Web Log Start ========================================\n");
        sb.append("| Service: ").append(logRecord.getServiceName()).append("\n");
        sb.append("| TraceID: ").append(logRecord.getTraceId()).append("\n");
        sb.append("| SpanID: ").append(logRecord.getSpanId()).append("\n");
        sb.append("| Event: ").append(logRecord.getEventName()).append("\n");
        sb.append("| Client IP: ").append(logRecord.getRequestIp()).append("\n");
        sb.append("| Status: ").append(logRecord.getStatus()).append("\n");
        sb.append("| Cost: ").append(logRecord.getCostTime()).append(" ms\n");
        sb.append("|----------------------------------------- Request ------------------------------------------\n");
        Object requestBodyObj = logRecord.getRequestBody();
        if (requestBodyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> requestInfoMap = (Map<String, Object>) requestBodyObj;
            String simplifiedRequestInfo = String.format("%s %s", requestInfoMap.get("method"), requestInfoMap.get("uri"));
            sb.append("| Request: ").append(simplifiedRequestInfo).append("\n");
            sb.append("| Request Details: ").append(JsonUtils.toJson(requestBodyObj)).append("\n");
        } else {
            sb.append("| Request Info: ").append(JsonUtils.toJson(requestBodyObj)).append("\n");
        }
        sb.append("| Request Headers: ").append(JsonUtils.toJson(logRecord.getRequestHeaders())).append("\n");
        sb.append("|----------------------------------------- Response -----------------------------------------\n");
        sb.append("| Response Body: ").append(JsonUtils.toJson(logRecord.getResponseBody())).append("\n");
        sb.append("| Response Headers: ").append(JsonUtils.toJson(logRecord.getResponseHeaders())).append("\n");

        if (logRecord.getExceptionStack() != null) {
            sb.append("|----------------------------------------- Exception ----------------------------------------\n");
            sb.append("| Exception: ").append(logRecord.getExceptionStack()).append("\n");
        }

        sb.append("========================================= Web Log End =========================================\n");
        return sb.toString();
    }

    /**
     * 截断内容到指定长度
     *
     * <p>
     * 如果内容长度超过指定的最大长度，则截断并添加省略号标识。
     * 用于控制日志内容的大小，避免过大的日志影响性能。
     * </p>
     *
     * @param content 原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    protected String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...[TRUNCATED]";
    }
    
    /**
     * 初始化忽略列表
     * 
     * <p>
     * 从配置属性中获取需要忽略的URL列表，并添加一些默认的系统URL。
     * 这些URL通常是健康检查、监控等不需要详细日志记录的接口。
     * </p>
     */
    @Override
    public void initIgnoreList() {
        // 从配置中获取忽略URL列表
        if (loggingProperties.getWeb() != null && 
            loggingProperties.getWeb().getIgnoreUrls() != null) {
            ignoreList.addAll(loggingProperties.getWeb().getIgnoreUrls());
        }
        
        // 添加默认忽略的系统URL
        ignoreList.addAll(Arrays.asList(
            "/actuator/**",
            "/health",
            "/info",
            "/metrics",
            "/favicon.ico",
            "/error"
        ));
        
        log.info("Web logging aspect initialized with ignore URLs: {}", ignoreList);
    }

}