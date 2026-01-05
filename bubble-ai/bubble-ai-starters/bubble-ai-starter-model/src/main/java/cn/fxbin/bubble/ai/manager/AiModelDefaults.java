package cn.fxbin.bubble.ai.manager;

import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.hutool.core.util.StrUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 模型默认配置
 *
 * @author fxbin
 * @since 2026-01-05 21:42:00
 */
public class AiModelDefaults {

    private static final Map<AiPlatformEnum, String> DEFAULT_MODELS = new ConcurrentHashMap<>();

    static {
        DEFAULT_MODELS.put(AiPlatformEnum.OPENAI, "gpt-5-mini");
        DEFAULT_MODELS.put(AiPlatformEnum.ANTHROPIC, "claude-sonnet-4-5");
        DEFAULT_MODELS.put(AiPlatformEnum.GEMINI, "gemini-3-flash-preview");
        DEFAULT_MODELS.put(AiPlatformEnum.DEEPSEEK, "deepseek-chat");
        DEFAULT_MODELS.put(AiPlatformEnum.ZHIPU, "glm-4.7");
        DEFAULT_MODELS.put(AiPlatformEnum.OLLAMA, "llama3.2");
        DEFAULT_MODELS.put(AiPlatformEnum.MINIMAX, "MiniMax-M2.1");
        DEFAULT_MODELS.put(AiPlatformEnum.SILICONFLOW, "deepseek-ai/DeepSeek-V3.2");
    }

    /**
     * 获取默认模型名称
     *
     * @param platform 平台
     * @return 默认模型名称
     */
    public static String getDefaultModel(AiPlatformEnum platform) {
        return DEFAULT_MODELS.getOrDefault(platform, "unknown-model");
    }

    /**
     * 解析模型名称 (如果为空或 "-", 则返回默认值)
     *
     * @param platform  平台
     * @param modelName 配置的模型名称
     * @return 实际使用的模型名称
     */
    public static String resolveModelName(AiPlatformEnum platform, String modelName) {
        if (StrUtil.isBlank(modelName) || "-".equals(modelName)) {
            return getDefaultModel(platform);
        }
        return modelName;
    }

}
