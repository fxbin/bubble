package cn.fxbin.bubble.ai.factory;

import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import org.springframework.ai.chat.model.ChatModel;

/**
 * AI 模型工厂接口
 *
 * @author fxbin
 */
public interface AiModelFactory {

    /**
     * 获取或创建 ChatModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @return ChatModel 实例
     */
    ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url);

    default ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature) {
        return getOrCreateChatModel(platform, apiKey, url, model, temperature, null);
    }

    ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK);

    /**
     * 获取默认配置的 ChatModel
     *
     * @param platform 平台枚举
     * @return ChatModel 实例
     */
    ChatModel getDefaultChatModel(AiPlatformEnum platform);

    /**
     * 根据配置 ID 获取 ChatModel
     *
     * @param providerId 配置 ID (bubble.ai.providers.{providerId})
     * @return ChatModel 实例
     * @throws IllegalArgumentException 如果找不到对应的配置
     */
    ChatModel getChatModel(String providerId);
}
