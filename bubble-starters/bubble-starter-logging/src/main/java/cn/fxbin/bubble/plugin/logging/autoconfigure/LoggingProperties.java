package cn.fxbin.bubble.plugin.logging.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志配置属性类
 * <p>
 * 提供日志系统的全局配置选项，包括Web层和Service层的日志开关控制
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 18:26
 */
@Data
@ConfigurationProperties(prefix = "bubble.logging")
public class LoggingProperties {

    /**
     * 是否启用日志功能
     * 默认值：true
     */
    private boolean enabled = true;

    /**
     * Web层日志配置
     */
    private Web web = new Web();

    /**
     * Service层日志配置
     */
    private Service service = new Service();

    /**
     * Web层日志配置类
     */
    @Data
    public static class Web {
        /**
         * 是否启用Web层日志
         * 默认值：true
         */
        private boolean enabled = true;

        /**
         * 需要忽略的URL路径列表
         * 支持Ant风格的路径匹配模式
         * 例如：/health, /actuator/**, /*.css, /*.js
         */
        private List<String> ignoreUrls = new ArrayList<>();

        /**
         * 需要过滤的敏感请求头列表
         * 这些请求头的值在日志中将被脱敏处理
         */
        private List<String> sensitiveHeaders = new ArrayList<String>() {{
            add("authorization");
            add("cookie");
            add("x-auth-token");
            add("x-api-key");
        }};

        /**
         * 是否记录请求体内容
         * 默认值：true
         */
        private boolean logRequestBody = true;

        /**
         * 是否记录响应体内容
         * 默认值：true
         */
        private boolean logResponseBody = true;

        /**
         * 请求体内容最大记录长度（字符数）
         * 超过此长度的内容将被截断
         * 默认值：1000
         */
        private int maxRequestBodyLength = 1000;

        /**
         * 响应体内容最大记录长度（字符数）
         * 超过此长度的内容将被截断
         * 默认值：1000
         */
        private int maxResponseBodyLength = 1000;
    }

    /**
     * Service层日志配置类
     */
    @Data
    public static class Service {
        /**
         * 是否启用Service层日志
         * 默认值：true
         */
        private boolean enabled = true;

        /**
         * 是否记录方法参数
         * 默认值：true
         */
        private boolean logParameters = true;

        /**
         * 是否记录方法返回值
         * 默认值：true
         */
        private boolean logReturnValue = true;

        /**
         * 方法参数最大记录长度（字符数）
         * 超过此长度的内容将被截断
         * 默认值：500
         */
        private int maxParameterLength = 500;

        /**
         * 方法返回值最大记录长度（字符数）
         * 超过此长度的内容将被截断
         * 默认值：500
         */
        private int maxReturnValueLength = 500;

        /**
         * 慢方法执行时间阈值（毫秒）
         * 超过此阈值的方法执行将被标记为慢方法
         * 默认值：1000ms
         */
        private long slowMethodThreshold = 1000L;
    }
}