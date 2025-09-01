package cn.fxbin.bubble.ai.lightrag.autoconfigure;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * LightRAG 服务配置属性类
 * 
 * <p>该类定义了 LightRAG 服务的核心配置参数，专注于必需的基础配置。</p>
 * 
 * <h3>配置示例：</h3>
 * <pre>{@code
 * dm:
 *   ai:
 *     lightrag:
 *       enabled: true
 *       base-url: "http://localhost:8020"
 *       timeout: 30s
 *       max-retries: 3
 *       api-key: "your-api-key"
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Data
@Validated
@ConfigurationProperties(prefix = "dm.ai.lightrag")
public class LightRagProperties {

    /**
     * 是否启用 LightRAG 服务
     * 默认值：true
     */
    private boolean enabled = true;

    /**
     * LightRAG 服务的基础 URL
     * 必须配置，用于指定 LightRAG 服务的访问地址
     * 
     * 示例：http://localhost:8020 或 https://lightrag.example.com
     */
    @NotBlank(message = "LightRAG 服务基础 URL 不能为空")
    private String baseUrl;

    /**
     * 请求超时时间
     * 默认值：30秒
     */
    @NotNull(message = "超时时间不能为空")
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * 最大重试次数
     * 默认值：3次
     * 取值范围：0-5
     */
    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 5, message = "重试次数不能大于5")
    private int maxRetries = 3;

    /**
     * API密钥
     * 用于API认证
     */
    private String apiKey;


}