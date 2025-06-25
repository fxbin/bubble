package cn.fxbin.bubble.plugin.logging.aspect;

import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.plugin.logging.model.SysLogRecord;
import cn.fxbin.bubble.plugin.logging.util.LogFactory;
import cn.fxbin.bubble.plugin.logging.util.TracerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * AbstractLogAspect
 *
 * <p>
 * 抽象日志切面基类，为具体的日志切面实现提供通用功能和模板方法。
 * 定义了日志记录的核心流程和通用的参数处理逻辑。
 * </p>
 *
 * <p>
 * 主要功能：
 * - 提供切点定义的抽象方法，由子类实现具体的切点逻辑
 * - 实现通用的日志记录模板方法，包括执行时间统计和异常处理
 * - 提供参数处理的通用逻辑，支持多种参数类型的序列化
 * - 集成分布式链路追踪，自动获取traceId和spanId
 * - 支持忽略列表的初始化，由子类定制具体的忽略规则
 * </p>
 *
 * <p>
 * 设计模式：
 * - 模板方法模式：定义日志记录的骨架流程，具体步骤由子类实现
 * - 策略模式：不同类型的切面可以有不同的切点和忽略策略
 * </p>
 *
 * <p>
 * 扩展点：
 * - pointCut()：定义具体的切点表达式
 * - initIgnoreList()：初始化需要忽略的URL或方法列表
 * - logRecord()：可重写以实现特定的日志记录逻辑
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/24 17:27
 */
@Slf4j
public abstract class AbstractLogAspect implements InitializingBean {

    /**
     * 忽略列表
     * 存储需要忽略日志记录的URL路径或方法名称
     * 在初始化时由子类通过initIgnoreList()方法填充
     */
    protected List<String> ignoreList = new ArrayList<>();

    /**
     * 定义切点表达式
     * 
     * <p>
     * 抽象方法，由具体的切面子类实现，用于定义该切面作用的范围。
     * 例如：@Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
     * </p>
     */
    public abstract void pointCut();

    /**
     * 初始化忽略列表
     * 
     * <p>
     * 抽象方法，由具体的切面子类实现，用于初始化需要忽略日志记录的
     * URL路径、方法名称或其他标识符列表。
     * </p>
     */
    public abstract void initIgnoreList();

    /**
     * 环绕通知 - 日志记录
     * 
     * <p>
     * 核心的日志记录方法，使用环绕通知拦截目标方法的执行。
     * 记录方法的执行时间、参数、返回值和异常信息。
     * </p>
     * 
     * @param joinPoint 连接点，包含被拦截方法的信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("pointCut()")
    public Object logRecord(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间（纳秒精度）
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
                
                // 构建日志记录
                SysLogRecord logRecord = buildLogRecord(joinPoint, startTime, endTime, 
                    startNs, endNs, costTime, result, exception);
                
                // 输出日志
                if (exception != null) {
                    log.error("Method execution failed: {}", JsonUtils.toJson(logRecord));
                } else {
                    log.info("Method execution completed: {}", JsonUtils.toJson(logRecord));
                }
            } catch (Exception e) {
                log.error("Error occurred while logging method execution", e);
            }
        }
    }

    /**
     * 构建日志记录对象
     * 
     * <p>
     * 根据方法执行的相关信息构建SysLogRecord对象，包含完整的执行上下文。
     * </p>
     * 
     * @param joinPoint 连接点信息
     * @param startTime 开始时间（毫秒）
     * @param endTime 结束时间（毫秒）
     * @param startNs 开始时间（纳秒）
     * @param endNs 结束时间（纳秒）
     * @param costTime 执行耗时（毫秒）
     * @param result 方法返回值
     * @param exception 异常信息
     * @return 构建完成的日志记录对象
     */
    protected SysLogRecord buildLogRecord(ProceedingJoinPoint joinPoint, long startTime, 
            long endTime, long startNs, long endNs, long costTime, Object result, Throwable exception) {
        
        return SysLogRecord.builder()
                .serviceName(getServiceName())
                .traceId(getTraceId())
                .spanId(getSpanId())
                .eventName(getEventName(joinPoint))
                .requestBody(processParameters(joinPoint.getArgs()))
                .responseBody(processResult(result))
                .costTime(costTime)
                .startNs(startNs)
                .endNs(endNs)
                .exceptionStack(exception != null ? getExceptionStack(exception) : null)
                .build();
    }

    /**
     * 获取服务名称
     * 
     * @return 服务名称，来源于LogFactory
     */
    protected String getServiceName() {
        try {
            return LogFactory.class.newInstance().getServiceId();
        } catch (Exception e) {
            log.warn("Failed to get service name from LogFactory", e);
            return "unknown-service";
        }
    }

    /**
     * 获取链路追踪ID
     * 
     * <p>
     * 从SOFATracer上下文中获取当前请求的traceId，用于分布式链路追踪。
     * 如果获取失败，返回null，不影响正常的业务逻辑执行。
     * </p>
     * 
     * @return 链路追踪ID，如果获取失败则返回null
     */
    protected String getTraceId() {
        try {
            return TracerUtils.getTraceId();
        } catch (Exception e) {
            log.debug("Failed to get traceId from SOFATracer", e);
            return null;
        }
    }

    /**
     * 获取跨度ID
     * 
     * <p>
     * 从SOFATracer上下文中获取当前请求的spanId，用于分布式链路追踪。
     * </p>
     * 
     * @return 跨度ID，如果获取失败则返回null
     */
    protected String getSpanId() {
        try {
            return TracerUtils.getSpanId();
        } catch (Exception e) {
            log.debug("Failed to get spanId from SOFATracer", e);
            return null;
        }
    }

    /**
     * 获取事件名称
     * 
     * <p>
     * 根据连接点信息生成事件名称，格式为：类名.方法名
     * </p>
     * 
     * @param joinPoint 连接点
     * @return 事件名称
     */
    protected String getEventName(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    /**
     * 处理方法参数
     * 
     * <p>
     * 将方法参数转换为可序列化的格式，过滤掉不适合记录的参数类型。
     * 支持多种参数类型的处理，包括HTTP请求相关的对象。
     * </p>
     * 
     * @param args 方法参数数组
     * @return 处理后的参数对象，可用于JSON序列化
     */
    protected Object processParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        List<Object> processedArgs = new ArrayList<>();
        
        for (Object arg : args) {
            if (arg == null) {
                processedArgs.add(null);
            } else if (arg instanceof HttpServletRequest) {
                // HTTP请求对象的特殊处理
                processedArgs.add(processHttpServletRequest((HttpServletRequest) arg));
            } else if (arg instanceof HttpServletResponse) {
                // HTTP响应对象不记录详细信息
                processedArgs.add("[HttpServletResponse]");
            } else if (arg instanceof MultipartFile) {
                // 文件上传对象的特殊处理
                processedArgs.add(processMultipartFile((MultipartFile) arg));
            } else {
                try {
                    // 尝试序列化普通对象
                    processedArgs.add(arg);
                } catch (Exception e) {
                    // 序列化失败时记录类型信息
                    processedArgs.add("[" + arg.getClass().getSimpleName() + "]");
                }
            }
        }
        
        return processedArgs.size() == 1 ? processedArgs.get(0) : processedArgs;
    }

    /**
     * 处理HTTP请求对象
     * 
     * <p>
     * 提取HTTP请求的关键信息，包括请求方法、URI、参数等。
     * 避免记录敏感信息和过大的对象。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return 包含请求关键信息的Map
     */
    protected Map<String, Object> processHttpServletRequest(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("uri", request.getRequestURI());
        requestInfo.put("queryString", request.getQueryString());
        
        // 处理请求参数
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
        
        return requestInfo;
    }

    /**
     * 处理文件上传对象
     * 
     * <p>
     * 提取文件上传的基本信息，不记录文件内容以避免日志过大。
     * </p>
     * 
     * @param file 文件上传对象
     * @return 包含文件基本信息的Map
     */
    protected Map<String, Object> processMultipartFile(MultipartFile file) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("originalFilename", file.getOriginalFilename());
        fileInfo.put("size", file.getSize());
        fileInfo.put("contentType", file.getContentType());
        return fileInfo;
    }

    /**
     * 处理方法返回值
     * 
     * <p>
     * 将方法返回值转换为可序列化的格式。
     * 对于无法序列化的对象，记录其类型信息。
     * </p>
     * 
     * @param result 方法返回值
     * @return 处理后的返回值对象
     */
    protected Object processResult(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            // 尝试序列化检查
            JsonUtils.toJson(result);
            return result;
        } catch (Exception e) {
            // 序列化失败时返回类型信息
            return "[" + result.getClass().getSimpleName() + "]";
        }
    }

    /**
     * 获取异常堆栈信息
     * 
     * <p>
     * 将异常信息转换为字符串格式，便于日志记录和问题排查。
     * 限制堆栈信息的长度，避免日志过大。
     * </p>
     * 
     * @param throwable 异常对象
     * @return 异常堆栈的字符串表示
     */
    protected String getExceptionStack(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        if (StringUtils.isNotBlank(throwable.getMessage())) {
            sb.append(": ").append(throwable.getMessage());
        }
        
        // 添加关键的堆栈信息（限制数量）
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int maxStackLines = Math.min(10, stackTrace.length);
        for (int i = 0; i < maxStackLines; i++) {
            sb.append("\n\tat ").append(stackTrace[i].toString());
        }
        
        if (stackTrace.length > maxStackLines) {
            sb.append("\n\t... ").append(stackTrace.length - maxStackLines).append(" more");
        }
        
        return sb.toString();
    }

    /**
     * Bean初始化完成后的回调
     * 
     * <p>
     * 在Bean的所有属性设置完成后调用，用于执行初始化逻辑。
     * 主要用于调用子类的initIgnoreList()方法来初始化忽略列表。
     * </p>
     * 
     * @throws Exception 初始化过程中发生的异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initIgnoreList();
        log.info("LogAspect initialized with ignore list: {}", ignoreList);
    }

}