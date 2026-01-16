package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * AI 模型创建器接口
 * <p>定义了创建ChatModel和EmbeddingModel的统一接口</p>
 *
 * @author fxbin
 */
public interface AiModelCreator {

    /**
     * Create Chat Model
     *
     * @param platform    AI Platform
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       Model Name
     * @param temperature Temperature
     * @param topK        Top K
     * @param topP        Top P
     * @return {@link ChatModel}
     */
    ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP);

    /**
     * Create Embedding Model
     *
     * @param platform   AI Platform
     * @param apiKey     API Key
     * @param baseUrl    Base URL
     * @param model      Model Name
     * @param dimensions Dimensions
     * @return {@link EmbeddingModel}
     */
    EmbeddingModel createEmbeddingModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Integer dimensions);

}
