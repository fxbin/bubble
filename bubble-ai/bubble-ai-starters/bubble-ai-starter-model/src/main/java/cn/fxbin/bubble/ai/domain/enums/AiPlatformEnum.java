package cn.fxbin.bubble.ai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 平台枚举
 * <p>定义支持的AI平台类型</p>
 *
 * @author fxbin
 * @since 2024/05/28
 */
@Getter
@AllArgsConstructor
public enum AiPlatformEnum {

    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI"),
    /**
     * DeepSeek
     */
    DEEPSEEK("deepseek", "DeepSeek"),
    /**
     * Ollama
     */
    OLLAMA("ollama", "Ollama"),
    /**
     * Google Gemini
     */
    GEMINI("gemini", "Gemini"),
    /**
     * Anthropic Claude
     */
    ANTHROPIC("anthropic", "Anthropic"),
    /**
     * Zhipu AI
     */
    ZHIPU("zhipu", "ZhipuAI"),
    /**
     * Minimax
     */
    MINIMAX("minimax", "Minimax"),
    /**
     * SiliconFlow
     */
    SILICONFLOW("siliconflow", "SiliconFlow"),
    /**
     * Alibaba DashScope
     */
    DASHSCOPE("dashscope", "DashScope"),
    /**
     * Azure OpenAI
     */
    AZURE_OPENAI("azure_openai", "Azure OpenAI");

    /**
     * 平台代码
     */
    @EnumValue
    private final String code;
    
    /**
     * 平台名称
     */
    private final String name;

    /**
     * 根据代码获取平台枚举
     *
     * @param code 平台代码
     * @return 平台枚举
     */
    public static AiPlatformEnum getByCode(String code) {
        for (AiPlatformEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
