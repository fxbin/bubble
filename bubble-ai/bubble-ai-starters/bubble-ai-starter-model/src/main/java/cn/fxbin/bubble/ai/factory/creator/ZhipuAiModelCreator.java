package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ai.document.MetadataMode;

/**
 * ZhipuAI 模型创建器
 * <p>负责创建ZhipuAI平台的ChatModel和EmbeddingModel实例</p>
 *
 * @author fxbin
 */
public class ZhipuAiModelCreator extends AbstractAiModelCreator {

    public ZhipuAiModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        super(properties, toolCallingManager, observationRegistry, retryTemplate);
    }

    @Override
    public ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        String finalBaseUrl = StringUtils.isNotBlank(baseUrl) ? baseUrl : "https://open.bigmodel.cn/api/paas/";
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        WebClient.Builder webClientBuilder = createWebClientBuilder();
        
        ZhiPuAiApi api = ZhiPuAiApi.builder()
                .baseUrl(finalBaseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .build();
                
        ZhiPuAiChatOptions.Builder optionsBuilder = ZhiPuAiChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        applyTopPIfSupported(optionsBuilder, topP);
        ZhiPuAiChatOptions options = optionsBuilder.build();

        if (toolCallingManager != null && observationRegistry != null) {
            return new ZhiPuAiChatModel(api, options, toolCallingManager, retryTemplate, observationRegistry);
        } else if (observationRegistry != null) {
            return new ZhiPuAiChatModel(api, options, retryTemplate, observationRegistry);
        } else {
            return new ZhiPuAiChatModel(api, options, retryTemplate);
        }
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Integer dimensions) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        String finalBaseUrl = StringUtils.isNotBlank(baseUrl) ? baseUrl : "https://open.bigmodel.cn/api/paas/";
        RestClient.Builder restClientBuilder = createRestClientBuilder();
        WebClient.Builder webClientBuilder = createWebClientBuilder();
        
        ZhiPuAiApi api = ZhiPuAiApi.builder()
                .baseUrl(finalBaseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .build();
                
        ZhiPuAiEmbeddingOptions options = ZhiPuAiEmbeddingOptions.builder().model(model).build();

        if (observationRegistry != null) {
            return new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED, options, retryTemplate, observationRegistry);
        } else {
            return new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED, options, retryTemplate);
        }
    }
}
