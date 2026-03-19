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
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
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
    @Mock private ObjectProvider<OpenAiEmbeddingModel> openAiEmbeddingProvider;
    @Mock private ObjectProvider<OllamaEmbeddingModel> ollamaEmbeddingProvider;
    @Mock private ObjectProvider<ZhiPuAiEmbeddingModel> zhipuEmbeddingProvider;
    @Mock private ObjectProvider<MiniMaxEmbeddingModel> minimaxEmbeddingProvider;
    @Mock private ObjectProvider<AzureOpenAiEmbeddingModel> azureOpenAiEmbeddingProvider;
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
                openAiEmbeddingProvider,
                ollamaEmbeddingProvider,
                zhipuEmbeddingProvider,
                minimaxEmbeddingProvider,
                azureOpenAiEmbeddingProvider,
                toolCallingManagerProvider,
                observationRegistryProvider,
                retryTemplateProvider
        );
    }

    @Test
    void getOrCreateChatModel_DifferentTopP_ShouldReturnDifferentInstance() {
        properties.getTokenCounting().setEnabled(false);
        ChatModel a = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 10, 0.9);
        ChatModel b = factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "k", "https://example.com", "m1", 0.1, 10, 0.8);

        assertThat(a).isNotSameAs(b);
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
    void getChatModel_WithGroupId_ShouldReturnFailoverModel() {
        BubbleAiProperties.ProviderConfig a = new BubbleAiProperties.ProviderConfig();
        a.setPlatform(AiPlatformEnum.OPENAI);
        a.setApiKey("key-a");
        a.setModel("gpt-4");

        BubbleAiProperties.ProviderConfig b = new BubbleAiProperties.ProviderConfig();
        b.setPlatform(AiPlatformEnum.OPENAI);
        b.setApiKey("key-b");
        b.setModel("gpt-4");

        BubbleAiProperties.ModelGroupConfig group = new BubbleAiProperties.ModelGroupConfig();
        BubbleAiProperties.GroupMemberConfig memberA = new BubbleAiProperties.GroupMemberConfig();
        memberA.setPriority(1);
        BubbleAiProperties.GroupMemberConfig memberB = new BubbleAiProperties.GroupMemberConfig();
        memberB.setPriority(2);
        group.getMembers().put("provider-a", memberA);
        group.getMembers().put("provider-b", memberB);

        properties.getProviders().put("provider-a", a);
        properties.getProviders().put("provider-b", b);
        properties.getGroups().put("openai-group", group);

        ChatModel result = factory.getChatModel("openai-group");

        assertThat(result).isInstanceOf(FailoverChatModel.class);
    }

    @Test
    void getChatModel_WithProviderGroupFallback_ShouldReturnFailoverModel() {
        BubbleAiProperties.ProviderConfig a = new BubbleAiProperties.ProviderConfig();
        a.setPlatform(AiPlatformEnum.OPENAI);
        a.setApiKey("key-a");
        a.setModel("gpt-4");
        a.setGroupId("openai-group");

        BubbleAiProperties.ProviderConfig b = new BubbleAiProperties.ProviderConfig();
        b.setPlatform(AiPlatformEnum.OPENAI);
        b.setApiKey("key-b");
        b.setModel("gpt-4");
        b.setGroupId("openai-group");

        BubbleAiProperties.ModelGroupConfig group = new BubbleAiProperties.ModelGroupConfig();
        BubbleAiProperties.GroupMemberConfig memberA = new BubbleAiProperties.GroupMemberConfig();
        memberA.setPriority(1);
        BubbleAiProperties.GroupMemberConfig memberB = new BubbleAiProperties.GroupMemberConfig();
        memberB.setPriority(2);
        group.getMembers().put("provider-a", memberA);
        group.getMembers().put("provider-b", memberB);

        properties.getProviders().put("provider-a", a);
        properties.getProviders().put("provider-b", b);
        properties.getGroups().put("openai-group", group);

        ChatModel result = factory.getChatModel("provider-a");

        assertThat(result).isInstanceOf(FailoverChatModel.class);
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
