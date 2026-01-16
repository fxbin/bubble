package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

/**
 * Anthropic 模型创建器
 * <p>负责创建Anthropic平台的ChatModel实例</p>
 *
 * @author fxbin
 */
public class AnthropicModelCreator extends AbstractAiModelCreator {

    public AnthropicModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        super(properties, toolCallingManager, observationRegistry, retryTemplate);
    }

    @Override
    public ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "https://api.anthropic.com";
        }

        AnthropicApi.Builder apiBuilder = AnthropicApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl);
        
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        applyMethodIfPresent(apiBuilder, "restClient", RestClient.Builder.class, restClientBuilder);
        
        AnthropicApi api = apiBuilder.build();
        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        applyTopPIfSupported(optionsBuilder, topP);
        AnthropicChatOptions options = optionsBuilder.build();

        AnthropicChatModel.Builder builder = AnthropicChatModel.builder()
                .anthropicApi(api)
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
}
