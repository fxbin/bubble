package cn.fxbin.bubble.ai.benchmark;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.factory.AiModelFactoryImpl;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.token.TokenCountingChatModel;
import cn.fxbin.bubble.ai.token.TokenUsageRecorder;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StopWatch;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Bubble AI Performance Benchmark Test
 *
 * @author fxbin
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class PerformanceBenchmarkTest {

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
        when(retryTemplateProvider.getIfAvailable(any(Supplier.class))).thenReturn(RetryUtils.DEFAULT_RETRY_TEMPLATE);

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
    void benchmarkModelCreationCaching() {
        // Setup
        when(openAiProvider.getIfAvailable()).thenReturn(openAiChatModel);
        // DeepSeek is created manually via API Key
        // when(deepSeekProvider.getIfAvailable()).thenReturn(mock(DeepSeekChatModel.class)); 
        
        properties.getTokenCounting().setEnabled(false); // Disable wrapper for pure factory test

        StopWatch sw = new StopWatch("Model Creation Benchmark");

        // Warmup
        for (int i = 0; i < 100; i++) {
             factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "key", "url", "model", 0.7);
        }

        int iterations = 10000;
        
        sw.start("Cached Access (OpenAI)");
        for (int i = 0; i < iterations; i++) {
            factory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "key", "url", "model", 0.7);
        }
        sw.stop();
        
        sw.start("Cached Access (DeepSeek)");
        for (int i = 0; i < iterations; i++) {
            // apiKey is required for DeepSeek
            factory.getOrCreateChatModel(AiPlatformEnum.DEEPSEEK, "ds-key", null, null, null);
        }
        sw.stop();

        log.info(sw.prettyPrint());
        System.out.println(sw.prettyPrint());
    }

    @Test
    void benchmarkTokenCountingOverhead() {
        // Setup
        ChatModel delegate = mock(ChatModel.class);
        ChatResponse response = mock(ChatResponse.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);
        
        when(usage.getTotalTokens()).thenReturn(100);
        when(metadata.getUsage()).thenReturn(usage);
        when(response.getMetadata()).thenReturn(metadata);
        when(delegate.call(any(Prompt.class))).thenReturn(response);

        TokenCountingChatModel wrapper = new TokenCountingChatModel(delegate, tokenUsageRecorder, tokenCountEstimator, "test", "model");
        Prompt prompt = new Prompt("test");

        int iterations = 10000;
        StopWatch sw = new StopWatch("Token Counting Overhead");

        // Warmup
        for(int i=0; i<100; i++) {
            delegate.call(prompt);
        }

        sw.start("Direct Call");
        for (int i = 0; i < iterations; i++) {
            delegate.call(prompt);
        }
        sw.stop();

        sw.start("Wrapped Call (Valid Usage)");
        for (int i = 0; i < iterations; i++) {
            wrapper.call(prompt);
        }
        sw.stop();

        // Verify async recording happened (at least once)
        verify(tokenUsageRecorder, timeout(1000).atLeastOnce()).record(any());

        // Test overhead when usage is missing (Trigger estimation)
        when(usage.getTotalTokens()).thenReturn(0); // Invalid usage
        when(tokenCountEstimator.estimate(any(String.class))).thenReturn(50);
        Generation generation = mock(Generation.class);
        when(generation.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage("response"));
        when(response.getResult()).thenReturn(generation);

        sw.start("Wrapped Call (Estimation Fallback)");
        for (int i = 0; i < iterations; i++) {
            wrapper.call(prompt);
        }
        sw.stop();

        log.info(sw.prettyPrint());
        System.out.println(sw.prettyPrint());
    }
}
