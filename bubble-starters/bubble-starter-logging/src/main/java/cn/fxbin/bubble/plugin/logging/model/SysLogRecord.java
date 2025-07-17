package cn.fxbin.bubble.plugin.logging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * SysLogRecord
 *
 * <p>
 * 系统日志记录模型类，用于封装系统运行过程中的日志信息。
 * 包含请求响应的完整生命周期信息，支持链路追踪和性能监控。
 * 适用于Web接口调用和Service方法调用的日志记录场景。
 * </p>
 *
 * <p>
 * 主要功能：
 * - 记录请求和响应的详细信息
 * - 支持分布式链路追踪（traceId、spanId）
 * - 记录方法执行耗时和异常信息
 * - 提供灵活的数据结构用于日志分析
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/24 17:27
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SysLogRecord implements Serializable {

    /**
     * 服务名称
     * 标识当前日志记录所属的服务实例名称
     */
    private String serviceName;

    /**
     * 链路追踪ID
     * 用于标识一次完整的请求链路，便于分布式系统中的日志关联分析
     */
    private String traceId;

    /**
     * 跨度ID
     * 用于标识当前操作在整个链路中的位置，配合traceId实现精确的链路追踪
     */
    private String spanId;

    /**
     * 事件名称
     * 根据不同场景记录不同的标识：
     * - Web接口：记录请求URI（如：/api/user/list）
     * - Service方法：记录完整方法名（如：com.example.UserService#getUserList）
     */
    private String eventName;

    /**
     * 请求IP地址
     * 记录发起请求的客户端IP地址，用于安全审计和访问分析
     */
    private String requestIp;

    /**
     * 接口响应时间
     * 记录方法或接口的执行耗时，单位：毫秒（ms）
     * 用于性能监控和性能瓶颈分析
     */
    private long costTime;

    /**
     * 请求体内容
     * 记录请求的参数信息，支持多种数据类型的序列化
     * 包括：基本类型、对象、集合等
     */
    private Object requestBody;

    /**
     * 请求头信息
     * 记录HTTP请求头的键值对信息
     * 用于分析请求来源、认证信息等
     */
    private Map<String, ?> requestHeaders;

    /**
     * 响应体内容
     * 记录方法或接口的返回值信息
     * 支持复杂对象的JSON序列化
     */
    private Object responseBody;

    /**
     * 响应头信息
     * 记录HTTP响应头的键值对信息
     * 包含内容类型、缓存策略等响应元数据
     */
    private Map<String, ?> responseHeaders;

    /**
     * HTTP状态码
     * 记录HTTP响应的状态码（如：200、404、500等）
     * 用于快速识别请求处理结果
     */
    private int status;

    /**
     * 异常堆栈信息
     * 当方法执行过程中发生异常时，记录完整的异常堆栈信息
     * 便于问题定位和错误分析
     */
    private String exceptionStack;

    /**
     * 开始时间戳
     * 记录方法或接口开始执行的时间戳，单位：毫秒（ms）
     */
    private Long startNs;

    /**
     * 结束时间戳
     * 记录方法或接口执行结束的时间戳，单位：毫秒（ms）
     */
    private Long endNs;

}