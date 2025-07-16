package cn.fxbin.bubble.plugin.logging.aspect;

import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.plugin.logging.autoconfigure.LoggingProperties;
import cn.fxbin.bubble.plugin.logging.model.SysLogRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * LogServiceAspect
 *
 * <p>
 * 服务层日志切面，专门用于记录业务服务方法的执行日志。
 * 继承自AbstractLogAspect，复用通用的日志记录逻辑，并针对服务层进行特化处理。
 * </p>
 *
 * <p>
 * 主要功能：
 * - 拦截所有@Service、@DubboService和@Component注解的类中的方法
 * - 记录服务方法的执行时间、参数和返回值
 * - 提供详细的异常信息记录和堆栈跟踪
 * - 集成分布式链路追踪，记录traceId和spanId
 * - 支持Dubbo服务的日志记录
 * </p>
 *
 * <p>
 * 配置说明：
 * - 通过bubble.logging.service.enabled控制是否启用服务层日志
 * - 默认启用，可通过配置关闭
 * - 仅拦截cn.fxbin.bubble包下的服务类
 * </p>
 *
 * <p>
 * 性能考虑：
 * - 使用@Order注解控制切面执行顺序，优先级低于Web层
 * - 高效的参数序列化处理
 * - 异常情况下的快速失败机制
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/24 17:27
 */
@Slf4j
@Aspect
@Order(2)
@RequiredArgsConstructor
public class LogServiceAspect extends AbstractLogAspect {

    /**
     * 日志配置属性
     * 用于获取服务层日志的相关配置
     */
    private final LoggingProperties loggingProperties;

    /**
     * 定义服务层切点
     * 
     * <p>
     * 拦截cn.fxbin.bubble包下所有标注了@Service、@DubboService或@Component注解的类中的方法。
     * 这样可以确保所有的业务服务方法都会被日志记录，包括Dubbo远程服务。
     * </p>
     */
    @Pointcut("(within(cn.fxbin.bubble..*) && (@within(org.springframework.stereotype.Service) || @within(org.apache.dubbo.config.annotation.DubboService) || @within(org.springframework.stereotype.Component)))")
    @Override
    public void pointCut() {
        // 切点定义，实际逻辑在注解中
    }

    /**
     * 服务层日志记录的环绕通知
     * 
     * <p>
     * 重写父类的logRecord方法，针对服务层进行优化：
     * - 使用高精度时间测量（纳秒级别）
     * - 专门的服务层日志格式
     * - 优化的异常处理和堆栈记录
     * - 线程安全的上下文清理
     * </p>
     * 
     * @param joinPoint 连接点，包含被拦截方法的信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("pointCut()")
    @Override
    public Object logRecord(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间（使用高精度计时）
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
                
                // 构建服务层专用的日志记录
                SysLogRecord logRecord = buildServiceLogRecord(joinPoint, 
                    startTime, endTime, startNs, endNs, costTime, result, exception);
                
                // 输出日志（根据执行情况和性能选择日志级别）
                if (exception != null) {
                    log.error("Service method execution failed: {}", JsonUtils.toJson(logRecord));
                } else if (isSlowMethod(costTime)) {
                    log.warn("Slow service method execution detected: {}", JsonUtils.toJson(logRecord));
                } else {
                    log.info("Service method execution completed: {}", JsonUtils.toJson(logRecord));
                }
            } catch (Exception e) {
                log.error("Error occurred while logging service method execution", e);
            }
        }
    }

    /**
     * 构建服务层专用的日志记录
     * 
     * <p>
     * 在父类基础日志记录的基础上，针对服务层进行优化：
     * - 更精确的执行时间统计
     * - 服务层特有的事件命名
     * - 优化的参数和返回值处理
     * - 增强的异常信息记录
     * - 根据配置控制参数和返回值记录
     * - 慢方法检测和标识
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
     * @return 构建完成的服务层日志记录对象
     */
    protected SysLogRecord buildServiceLogRecord(ProceedingJoinPoint joinPoint, 
            long startTime, long endTime, long startNs, long endNs, 
            long costTime, Object result, Throwable exception) {
        
        // 处理方法参数（根据配置决定是否记录）
        Object requestBody = null;
        if (shouldLogParameters()) {
            Object processedArgs = processParameters(joinPoint.getArgs());
            if (processedArgs != null) {
                String argsJson = JsonUtils.toJson(processedArgs);
                requestBody = truncateContent(argsJson, getMaxParameterLength());
            }
        }
        
        // 处理返回值（根据配置决定是否记录）
        Object responseBody = null;
        if (shouldLogReturnValue()) {
            Object processedResult = processResult(result);
            if (processedResult != null) {
                String resultJson = JsonUtils.toJson(processedResult);
                responseBody = truncateContent(resultJson, getMaxReturnValueLength());
            }
        }
        
        // 构建事件名称，如果是慢方法则添加标识
        String eventName = getServiceEventName(joinPoint);
        if (isSlowMethod(costTime)) {
            eventName = "[SLOW] " + eventName;
        }
        
        return SysLogRecord.builder()
                .serviceName(getServiceName())
                .traceId(getTraceId())
                .spanId(getSpanId())
                .eventName(eventName)
                .requestBody(requestBody)
                .responseBody(responseBody)
                .costTime(costTime)
                .startNs(startNs)
                .endNs(endNs)
                .exceptionStack(exception != null ? getExceptionStack(exception) : null)
                .build();
    }

    /**
     * 生成服务层事件名称
     * 
     * <p>
     * 为服务层方法生成更具描述性的事件名称，包含服务类型信息。
     * 格式：[SERVICE] ClassName.methodName
     * </p>
     * 
     * @param joinPoint 连接点
     * @return 服务层事件名称
     */
    protected String getServiceEventName(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 判断服务类型
        String serviceType = "SERVICE";
        Class<?> targetClass = joinPoint.getTarget().getClass();
        
        if (targetClass.isAnnotationPresent(DubboService.class)) {
            serviceType = "DUBBO_SERVICE";
        } else if (targetClass.isAnnotationPresent(Service.class)) {
            serviceType = "SPRING_SERVICE";
        } else if (targetClass.isAnnotationPresent(Component.class)) {
            serviceType = "COMPONENT";
        }
        
        return String.format("[%s] %s.%s", serviceType, className, methodName);
    }

    /**
     * 检查是否应该记录方法参数
     * 
     * @return true表示应该记录方法参数
     */
    protected boolean shouldLogParameters() {
        return loggingProperties.getService() != null && 
               loggingProperties.getService().isLogParameters();
    }
    
    /**
     * 检查是否应该记录方法返回值
     * 
     * @return true表示应该记录方法返回值
     */
    protected boolean shouldLogReturnValue() {
        return loggingProperties.getService() != null && 
               loggingProperties.getService().isLogReturnValue();
    }
    
    /**
     * 获取方法参数最大长度配置
     * 
     * @return 方法参数最大长度
     */
    protected int getMaxParameterLength() {
        return loggingProperties.getService() != null ? 
               loggingProperties.getService().getMaxParameterLength() : 500;
    }
    
    /**
     * 获取方法返回值最大长度配置
     * 
     * @return 方法返回值最大长度
     */
    protected int getMaxReturnValueLength() {
        return loggingProperties.getService() != null ? 
               loggingProperties.getService().getMaxReturnValueLength() : 500;
    }
    
    /**
     * 获取慢方法执行时间阈值
     * 
     * @return 慢方法阈值（毫秒）
     */
    protected long getSlowMethodThreshold() {
        return loggingProperties.getService() != null ? 
               loggingProperties.getService().getSlowMethodThreshold() : 1000L;
    }
    
    /**
     * 判断是否为慢方法
     * 
     * @param costTime 方法执行耗时（毫秒）
     * @return true表示是慢方法
     */
    protected boolean isSlowMethod(long costTime) {
        return costTime >= getSlowMethodThreshold();
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
     * 服务层通常不需要忽略特定的方法，因为业务服务方法都应该被记录。
     * 如果将来需要忽略某些方法，可以通过配置进行扩展。
     * </p>
     */
    @Override
    public void initIgnoreList() {
        // 服务层默认不忽略任何方法
        // 如果需要忽略特定方法，可以在这里添加逻辑
        
        // 示例：从配置中获取忽略的服务方法列表
        // if (loggingProperties.getService() != null && 
        //     loggingProperties.getService().getIgnoreMethods() != null) {
        //     ignoreList.addAll(loggingProperties.getService().getIgnoreMethods());
        // }
        
        log.info("Service logging aspect initialized with ignore list: {}", ignoreList);
    }

}