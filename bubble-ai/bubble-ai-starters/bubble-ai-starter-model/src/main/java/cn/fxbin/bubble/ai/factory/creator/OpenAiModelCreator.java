package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

/**
 * OpenAI 模型创建器
 * <p>负责创建OpenAI平台的ChatModel和EmbeddingModel实例</p>
 *
 * @author fxbin
 */
public class OpenAiModelCreator extends AbstractAiModelCreator {

    public OpenAiModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        super(properties, toolCallingManager, observationRegistry, retryTemplate);
    }

    @Override
    public ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        String finalBaseUrl = StringUtils.isNotBlank(baseUrl) ? baseUrl : "https://api.openai.com";
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(finalBaseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder)
                .build();
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder().model(model).temperature(temperature);
        applyTopPIfSupported(optionsBuilder, topP);
        OpenAiChatOptions options = optionsBuilder.build();

        OpenAiChatModel.Builder builder = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .retryTemplate(retryTemplate);
        if (toolCallingManager != null) {
            builder.toolCallingManager(toolCallingManager);
        }
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder.build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Integer dimensions) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        String finalBaseUrl = StringUtils.isNotBlank(baseUrl) ? baseUrl : "https://api.openai.com";
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(finalBaseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder)
                .build();
        OpenAiEmbeddingOptions.Builder optionsBuilder = OpenAiEmbeddingOptions.builder().model(model);
        if (dimensions != null) {
            optionsBuilder.dimensions(dimensions);
        }
        OpenAiEmbeddingOptions options = optionsBuilder.build();

        if (observationRegistry != null) {
            return new OpenAiEmbeddingModel(api, MetadataMode.EMBED, options, retryTemplate, observationRegistry);
        } else {
            return new OpenAiEmbeddingModel(api, MetadataMode.EMBED, options, retryTemplate);
        }
    }
}
