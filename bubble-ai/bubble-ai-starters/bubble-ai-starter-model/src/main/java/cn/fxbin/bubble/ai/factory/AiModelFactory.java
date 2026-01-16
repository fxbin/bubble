package cn.fxbin.bubble.ai.factory;

import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import reactor.core.publisher.Mono;

/**
 * AI 模型工厂接口
 * <p>负责创建和管理AI模型实例，支持同步和异步方式获取ChatModel和EmbeddingModel</p>
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
        return getOrCreateChatModel(platform, apiKey, url, model, temperature, null, null);
    }

    ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK);

    ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK, Double topP);

    /**
     * 移除 ChatModel 缓存
     *
     * @param platform    平台枚举
     * @param apiKey      API Key
     * @param url         Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @param topP        Top P
     */
    void removeChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK, Double topP);

    /**
     * 异步获取或创建 ChatModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @return ChatModel 实例的 Mono
     */
    default Mono<ChatModel> getOrCreateChatModelAsync(AiPlatformEnum platform, String apiKey, String url) {
        return Mono.fromCallable(() -> getOrCreateChatModel(platform, apiKey, url))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 异步获取或创建 ChatModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @param model    模型名称
     * @param temperature 温度
     * @return ChatModel 实例的 Mono
     */
    default Mono<ChatModel> getOrCreateChatModelAsync(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature) {
        return Mono.fromCallable(() -> getOrCreateChatModel(platform, apiKey, url, model, temperature))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 异步获取或创建 ChatModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @param model    模型名称
     * @param temperature 温度
     * @param topK     Top K
     * @return ChatModel 实例的 Mono
     */
    default Mono<ChatModel> getOrCreateChatModelAsync(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK) {
        return Mono.fromCallable(() -> getOrCreateChatModel(platform, apiKey, url, model, temperature, topK))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 获取或创建 EmbeddingModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @return EmbeddingModel 实例
     */
    EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 获取或创建 EmbeddingModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @param model    模型名称
     * @return EmbeddingModel 实例
     */
    EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model);

    /**
     * 获取或创建 EmbeddingModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @param model    模型名称
     * @param dimensions 维度
     * @return EmbeddingModel 实例
     */
    EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model, Integer dimensions);

    /**
     * 异步获取或创建 EmbeddingModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @return EmbeddingModel 实例的 Mono
     */
    default Mono<EmbeddingModel> getOrCreateEmbeddingModelAsync(AiPlatformEnum platform, String apiKey, String url) {
        return Mono.fromCallable(() -> getOrCreateEmbeddingModel(platform, apiKey, url))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 异步获取或创建 EmbeddingModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @param model    模型名称
     * @return EmbeddingModel 实例的 Mono
     */
    default Mono<EmbeddingModel> getOrCreateEmbeddingModelAsync(AiPlatformEnum platform, String apiKey, String url, String model) {
        return Mono.fromCallable(() -> getOrCreateEmbeddingModel(platform, apiKey, url, model))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 异步获取或创建 EmbeddingModel
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @param model    模型名称
     * @param dimensions 维度
     * @return EmbeddingModel 实例的 Mono
     */
    default Mono<EmbeddingModel> getOrCreateEmbeddingModelAsync(AiPlatformEnum platform, String apiKey, String url, String model, Integer dimensions) {
        return Mono.fromCallable(() -> getOrCreateEmbeddingModel(platform, apiKey, url, model, dimensions))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 获取默认配置的 ChatModel
     *
     * @param platform 平台枚举
     * @return ChatModel 实例
     */
    ChatModel getDefaultChatModel(AiPlatformEnum platform);

    /**
     * 异步获取默认配置的 ChatModel
     *
     * @param platform 平台枚举
     * @return ChatModel 实例的 Mono
     */
    default Mono<ChatModel> getDefaultChatModelAsync(AiPlatformEnum platform) {
        return Mono.fromCallable(() -> getDefaultChatModel(platform))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 获取默认配置的 EmbeddingModel
     *
     * @param platform 平台枚举
     * @return EmbeddingModel 实例
     */
    EmbeddingModel getDefaultEmbeddingModel(AiPlatformEnum platform);

    /**
     * 异步获取默认配置的 EmbeddingModel
     *
     * @param platform 平台枚举
     * @return EmbeddingModel 实例的 Mono
     */
    default Mono<EmbeddingModel> getDefaultEmbeddingModelAsync(AiPlatformEnum platform) {
        return Mono.fromCallable(() -> getDefaultEmbeddingModel(platform))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 根据配置 ID 获取 ChatModel
     *
     * @param modelId 模型ID (对应配置文件Key或数据库配置ID)
     * @return ChatModel 实例
     * @throws IllegalArgumentException 如果找不到对应的配置
     */
    ChatModel getChatModel(String modelId);

    /**
     * 异步根据配置 ID 获取 ChatModel
     *
     * @param modelId 模型ID (对应配置文件Key或数据库配置ID)
     * @return ChatModel 实例的 Mono
     */
    default Mono<ChatModel> getChatModelAsync(String modelId) {
        return Mono.fromCallable(() -> getChatModel(modelId))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 根据配置 ID 获取 EmbeddingModel
     *
     * @param modelId 模型ID (对应配置文件Key)
     * @return EmbeddingModel 实例
     * @throws IllegalArgumentException 如果找不到对应的配置
     */
    EmbeddingModel getEmbeddingModel(String modelId);

    /**
     * 异步根据配置 ID 获取 EmbeddingModel
     *
     * @param modelId 模型ID (对应配置文件Key)
     * @return EmbeddingModel 实例的 Mono
     */
    default Mono<EmbeddingModel> getEmbeddingModelAsync(String modelId) {
        return Mono.fromCallable(() -> getEmbeddingModel(modelId))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * 获取或创建 AgentScope Model (适配器模式)
     *
     * @param platform 平台枚举
     * @param apiKey   API Key (可选，覆盖配置)
     * @param url      Base URL (可选，覆盖配置)
     * @return AgentScope Model 实例
     */
    io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 获取或创建 AgentScope Model (适配器模式)
     *
     * @param platform    平台枚举
     * @param apiKey      API Key (可选，覆盖配置)
     * @param url         Base URL (可选，覆盖配置)
     * @param model       模型名称
     * @param temperature 温度
     * @return AgentScope Model 实例
     */
    default io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature) {
        return getOrCreateAgentScopeModel(platform, apiKey, url, model, temperature, null);
    }

    /**
     * 获取或创建 AgentScope Model (适配器模式)
     *
     * @param platform    平台枚举
     * @param apiKey      API Key (可选，覆盖配置)
     * @param url         Base URL (可选，覆盖配置)
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return AgentScope Model 实例
     */
    default io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK) {
        return getOrCreateAgentScopeModel(platform, apiKey, url, model, temperature, topK, null);
    }

    /**
     * 获取或创建 AgentScope Model (适配器模式)
     *
     * @param platform    平台枚举
     * @param apiKey      API Key (可选，覆盖配置)
     * @param url         Base URL (可选，覆盖配置)
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @param topP        Top P
     * @return AgentScope Model 实例
     */
    io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK, Double topP);

}
