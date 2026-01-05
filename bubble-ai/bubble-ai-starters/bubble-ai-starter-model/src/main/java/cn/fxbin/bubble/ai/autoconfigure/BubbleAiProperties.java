package cn.fxbin.bubble.ai.autoconfigure;

import cn.fxbin.bubble.ai.constants.AiModelConstants;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Bubble AI 配置属性
 *
 * @author fxbin
 */
@Data
@ConfigurationProperties(prefix = "bubble.ai")
public class BubbleAiProperties {

    /**
     * Token 计算配置
     */
    private TokenCounting tokenCounting = new TokenCounting();

    /**
     * 数据库模型配置
     */
    private ModelConfig modelConfig = new ModelConfig();

    /**
     * 多模型配置提供商
     * <p>Key: 自定义提供商ID (如: deepseek-v3, gpt-4-turbo)</p>
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    @Data
    public static class TokenCounting {
        /**
         * 是否启用 Token 计算
         */
        private boolean enabled = true;

        /**
         * 是否启用流式响应的估算功能
         * <p>开启后会缓冲流式响应内容用于 Token 估算，可能导致内存增加。
         * 如果 Provider 返回了 Usage 信息，则优先使用 Provider 的信息。</p>
         */
        private boolean streamEstimationEnabled = true;
    }

    @Data
    public static class ModelConfig {
        /**
         * 是否启用数据库配置支持
         * <p>默认关闭，需显式开启</p>
         */
        private boolean enabled = false;
    }

    @Data
    public static class ProviderConfig {
        /**
         * 平台类型
         */
        private AiPlatformEnum platform;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * Base URL
         */
        private String baseUrl;

        /**
         * 模型名称
         */
        private String model;

        /**
         * 模型描述
         */
        private String description;

        /**
         * 温度
         */
        private Double temperature = AiModelConstants.Model.DEFAULT_TEMPERATURE;

        /**
         * Top K
         */
        private Integer topK;
    }
}
