package cn.fxbin.bubble.flow.core.model.node;

import cn.fxbin.bubble.flow.core.model.InputParamDefinition;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * HttpNode 节点元数据
 * 用于配置化 HTTP 请求的节点定义
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:04
 */
@Data
public class HttpNodeMetadata implements Serializable {

    /**
     * 请求方法: GET, POST, PUT, DELETE 等
     */
    private String method;

    /**
     * 请求URL: 支持配置化参数替换,如 http://api.example.com/${path}
     */
    private String url;

    /**
     * 请求Content-Type: application/json, application/x-www-form-urlencoded 等
     */
    private String contentType;

    /**
     * 请求头参数列表: Authorization, Accept 等
     */
    private List<InputParamDefinition> headers;

    /**
     * URL查询参数列表: 会被拼接到url后作为查询字符串
     */
    private List<InputParamDefinition> params;

    /**
     * 请求体参数列表: 用于 form 表单提交
     */
    private List<InputParamDefinition> bodyParams;

    /**
     * JSON格式的请求体: 当 contentType 为 application/json 时使用
     */
    private String jsonBody;

    /**
     * 响应数据类型: json, text, binary 等
     */
    private String responseType;

    /**
     * 超时时间(毫秒)
     */
    private Integer timeout;

    /**
     * 是否允许重定向
     */
    private Boolean followRedirects;

    /**
     * 重试次数
     */
    private Integer retryCount;

}