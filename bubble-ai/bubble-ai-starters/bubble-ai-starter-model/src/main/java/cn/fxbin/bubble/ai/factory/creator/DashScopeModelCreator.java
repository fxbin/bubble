package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.core.util.StringUtils;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * DashScope 模型创建器
 * <p>负责创建阿里云DashScope平台的ChatModel实例</p>
 *
 * @author fxbin
 */
public class DashScopeModelCreator extends AbstractAiModelCreator {

    public DashScopeModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        super(properties, toolCallingManager, observationRegistry, retryTemplate);
    }

    @Override
    public ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        DashScopeApi.Builder apiBuilder = DashScopeApi.builder().apiKey(apiKey);
        if (StringUtils.isNotBlank(baseUrl)) {
            apiBuilder.baseUrl(baseUrl);
        }
        
        WebClient.Builder webClientBuilder = createWebClientBuilder();
        applyMethodIfPresent(apiBuilder, "webClientBuilder", WebClient.Builder.class, webClientBuilder);
        
        DashScopeApi api = apiBuilder.build();
        DashScopeChatOptions.DashScopeChatOptionsBuilder optionsBuilder = DashScopeChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        applyTopPIfSupported(optionsBuilder, topP);
        DashScopeChatOptions options = optionsBuilder.build();

        DashScopeChatModel.Builder builder = DashScopeChatModel.builder()
                .dashScopeApi(api)
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
