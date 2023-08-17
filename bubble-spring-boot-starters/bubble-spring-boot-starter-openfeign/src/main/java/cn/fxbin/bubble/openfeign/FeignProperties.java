package cn.fxbin.bubble.openfeign;

import feign.Logger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

import static cn.fxbin.bubble.openfeign.FeignProperties.BUBBLE_FIREWORKS_FEIGN_PREFIX;

/**
 * FeignGlobalProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/22 18:19
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = BUBBLE_FIREWORKS_FEIGN_PREFIX)
public class FeignProperties {

    /**
     * swagger prefix
     */
    public static final String BUBBLE_FIREWORKS_FEIGN_PREFIX = "bubble.fireworks.feign";

    /**
     * 默认的全局透传header 全局透传请求头：X-Real-IP x-forwarded-for 请求和转发的ip, Authorization
     */
    public static final String[] ALLOW_HEADERS = new String[]{
            "X-Real-IP", "x-forwarded-for", "Authorization"
    };

    /**
     * 允许透传的header
     */
    private List<String> allowHeaders = new ArrayList<>();

    /**
     * 连接超时时间，单位：毫秒
     */
    private long connectTimeout = 2000;

    /**
     * 读取超时，单位：毫秒
     */
    private long readTimeout = 2000;

    /**
     * 日志级别
     */
    private Logger.Level loggingLevel = Logger.Level.FULL;


}
