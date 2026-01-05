package cn.fxbin.bubble.ai.factory;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.token.TokenCountingChatModel;
import cn.fxbin.bubble.ai.token.TokenUsageRecorder;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.retry.support.RetryTemplate;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiModelFactoryTest {

    private BubbleAiProperties properties;

    @Mock private TokenCountEstimator tokenCountEstimator;
    @Mock private TokenUsageRecorder tokenUsageRecorder;
    @Mock private ObjectProvider<OpenAiChatModel> openAiProvider;
    @Mock private ObjectProvider<OllamaChatModel> ollamaProvider;
    @Mock private ObjectProvider<AnthropicChatModel> anthropicProvider;
    @Mock private ObjectProvider<VertexAiGeminiChatModel> geminiProvider;
    @Mock private ObjectProvider<DeepSeekChatModel> deepSeekProvider;
    @Mock private ObjectProvider<ZhiPuAiChatModel> zhipuProvider;
    @Mock private ObjectProvider<MiniMaxChatModel> minimaxProvider;
    @Mock private ObjectProvider<ToolCallingManager> toolCallingManagerProvider;
    @Mock private ObjectProvider<ObservationRegistry> observationRegistryProvider;
    @Mock private ObjectProvider<RetryTemplate> retryTemplateProvider;

    @Mock private OpenAiChatModel openAiChatModel;

    private AiModelFactoryImpl factory;

    @BeforeEach
    void setUp() {
        properties = new BubbleAiProperties();

        when(retryTemplateProvider.getIfAvailable(any(Supplier.class))).thenReturn(new RetryTemplate());

        factory = new AiModelFactoryImpl(
                properties,
                tokenUsageRecorder,
                tokenCountEstimator,
                openAiProvider,
                ollamaProvider,
                anthropicProvider,
                geminiProvider,
                deepSeekProvider,
                zhipuProvider,
                minimaxProvider,
                toolCallingManagerProvider,
                observationRegistryProvider,
                retryTemplateProvider
        );
    }

    @Test
    void getChatModel_WithProviderId_ShouldReturnConfiguredModel() {
        BubbleAiProperties.ProviderConfig config = new BubbleAiProperties.ProviderConfig();
        config.setPlatform(AiPlatformEnum.OPENAI);
        config.setApiKey("test-key");
        config.setModel("gpt-4");

        properties.getProviders().put("test-provider", config);

        ChatModel result = factory.getChatModel("test-provider");

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TokenCountingChatModel.class);
    }

    @Test
    void getChatModel_WithInvalidProviderId_ShouldThrowException() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            factory.getChatModel("");
        });
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            factory.getChatModel("non-existent-provider");
        });
    }

    @Test
    void getDefaultChatModel_OpenAi_ShouldReturnInjectedBeanWrapped() {
        when(openAiProvider.getIfAvailable()).thenReturn(openAiChatModel);

        ChatModel result = factory.getDefaultChatModel(AiPlatformEnum.OPENAI);

        assertThat(result).isInstanceOf(TokenCountingChatModel.class);
    }

    @Test
    void getOrCreateChatModel_WithEmptyParams_ShouldReturnDefault() {
        when(openAiProvider.getIfAvailable()).thenReturn(openAiChatModel);

        ChatModel result = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, null, "");

        assertThat(result).isInstanceOf(TokenCountingChatModel.class);
    }

    @Test
    void getDefaultChatModel_TokenCountingDisabled_ShouldReturnRawBean() {
        properties.getTokenCounting().setEnabled(false);
        when(openAiProvider.getIfAvailable()).thenReturn(openAiChatModel);

        ChatModel result = factory.getDefaultChatModel(AiPlatformEnum.OPENAI);

        assertThat(result).isSameAs(openAiChatModel);
    }

    @Test
    void getOrCreateChatModel_SameParams_ShouldReturnCachedInstance() {
        properties.getTokenCounting().setEnabled(false);
        ChatModel a = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 10);
        ChatModel b = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 10);

        assertThat(a).isSameAs(b);
    }

    @Test
    void getOrCreateChatModel_DifferentModel_ShouldReturnDifferentInstance() {
        properties.getTokenCounting().setEnabled(false);
        ChatModel a = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 10);
        ChatModel b = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m2", 0.1, 10);

        assertThat(a).isNotSameAs(b);
    }

    @Test
    void getOrCreateChatModel_DifferentTopK_ShouldReturnDifferentInstance() {
        properties.getTokenCounting().setEnabled(false);
        ChatModel a = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 10);
        ChatModel b = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 20);

        assertThat(a).isNotSameAs(b);
    }

}
