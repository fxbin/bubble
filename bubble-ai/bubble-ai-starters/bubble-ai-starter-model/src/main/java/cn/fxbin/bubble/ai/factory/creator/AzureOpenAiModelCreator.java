package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.core.util.StringUtils;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;

/**
 * Azure OpenAI 模型创建器
 * <p>负责创建Azure OpenAI平台的ChatModel和EmbeddingModel实例</p>
 *
 * @author fxbin
 */
public class AzureOpenAiModelCreator extends AbstractAiModelCreator {

    public AzureOpenAiModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        super(properties, toolCallingManager, observationRegistry, retryTemplate);
    }

    @Override
    public ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("baseUrl must not be blank for Azure OpenAI");
        }

        BubbleAiProperties.HttpTimeout timeout = properties.getHttpTimeout();
        
        com.azure.core.http.HttpClient azureHttpClient = new com.azure.core.http.netty.NettyAsyncHttpClientBuilder()
                .connectTimeout(java.time.Duration.ofMillis(timeout.getConnectTimeout()))
                .responseTimeout(java.time.Duration.ofMillis(timeout.getReadTimeout()))
                .build();

        OpenAIClientBuilder clientBuilder = new OpenAIClientBuilder()
                .credential(new KeyCredential(apiKey))
                .endpoint(baseUrl)
                .httpClient(azureHttpClient);

        AzureOpenAiChatOptions.Builder optionsBuilder = AzureOpenAiChatOptions.builder().deploymentName(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        applyTopPIfSupported(optionsBuilder, topP);
        AzureOpenAiChatOptions options = optionsBuilder.build();

        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .openAIClientBuilder(clientBuilder)
                .defaultOptions(options);
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
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("baseUrl must not be blank for Azure OpenAI");
        }

        BubbleAiProperties.HttpTimeout timeout = properties.getHttpTimeout();
        
        com.azure.core.http.HttpClient azureHttpClient = new com.azure.core.http.netty.NettyAsyncHttpClientBuilder()
                .connectTimeout(java.time.Duration.ofMillis(timeout.getConnectTimeout()))
                .responseTimeout(java.time.Duration.ofMillis(timeout.getReadTimeout()))
                .build();

        OpenAIClientBuilder clientBuilder = new OpenAIClientBuilder()
                .credential(new KeyCredential(apiKey))
                .endpoint(baseUrl)
                .httpClient(azureHttpClient);

        AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder()
                .deploymentName(model)
                .build();

        OpenAIClient openAIClient = clientBuilder.buildClient();

        if (observationRegistry != null) {
            return new AzureOpenAiEmbeddingModel(openAIClient, MetadataMode.EMBED, options, observationRegistry);
        } else {
            return new AzureOpenAiEmbeddingModel(openAIClient, MetadataMode.EMBED, options);
        }
    }
}
