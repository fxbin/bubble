package cn.fxbin.bubble.plugin.logging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * BubbleFireworksLogging
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/18 16:05
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BubbleFireworksLogging implements Serializable {

    private static final long serialVersionUID = -7174978100083929458L;

    /**
     * 应用名称, affected by "spring.application.name" config properties
     */
    private String serviceId;

    /**
     * 服务IP
     */
    private String serviceIp;

    /**
     * 服务端口, affected by "server.port" config properties
     */
    private String servicePort;

    /**
     * span id
     */
    private String spanId;

    /**
     * trace id
     */
    private String traceId;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求Uri
     */
    private String requestUri;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数内容
     */
    private String requestBody;

    /**
     * 请求 Headers
     */
    private Map<String, String> requestHeaders;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应内容
     */
    private String responseBody;

    /**
     * 响应 Headers
     */
    private Map<String, String> responseHeaders;

    /**
     * exception stack
     */
    private String exceptionStack;

    /**
     * 开始时间(ms)
     */
    private Long startRequestTime;

    /**
     * 结束时间(ms)
     */
    private Long endResponseTime;

    /**
     * 请求耗时(ms)
     */
    private Long timeConsuming;

}
