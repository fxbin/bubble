package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.ai.document.MetadataMode;
import org.springframework.web.client.RestClient;

/**
 * Minimax 模型创建器
 * <p>负责创建Minimax平台的ChatModel和EmbeddingModel实例</p>
 *
 * @author fxbin
 */
public class MinimaxModelCreator extends AbstractAiModelCreator {

    public MinimaxModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        super(properties, toolCallingManager, observationRegistry, retryTemplate);
    }

    @Override
    public ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        String finalBaseUrl = StringUtils.isNotBlank(baseUrl) ? baseUrl : "https://api.minimax.chat";
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        MiniMaxApi api = new MiniMaxApi(finalBaseUrl, apiKey, restClientBuilder);
        
        MiniMaxChatOptions.Builder optionsBuilder = MiniMaxChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        applyTopPIfSupported(optionsBuilder, topP);
        MiniMaxChatOptions options = optionsBuilder.build();

        if (toolCallingManager != null && observationRegistry != null) {
            return new MiniMaxChatModel(api, options, toolCallingManager, retryTemplate, observationRegistry, new DefaultToolExecutionEligibilityPredicate());
        } else if (observationRegistry != null) {
            return new MiniMaxChatModel(api, options, ToolCallingManager.builder().build(), retryTemplate, observationRegistry, new DefaultToolExecutionEligibilityPredicate());
        } else {
            return new MiniMaxChatModel(api, options, ToolCallingManager.builder().build(), retryTemplate);
        }
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Integer dimensions) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        String finalBaseUrl = StringUtils.isNotBlank(baseUrl) ? baseUrl : "https://api.minimax.chat";
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        MiniMaxApi api = new MiniMaxApi(finalBaseUrl, apiKey, restClientBuilder);
                
        MiniMaxEmbeddingOptions options = MiniMaxEmbeddingOptions.builder().model(model).build();

        if (observationRegistry != null) {
            return new MiniMaxEmbeddingModel(api, MetadataMode.EMBED, options, retryTemplate, observationRegistry);
        } else {
            return new MiniMaxEmbeddingModel(api, MetadataMode.EMBED, options, retryTemplate);
        }
    }
}
