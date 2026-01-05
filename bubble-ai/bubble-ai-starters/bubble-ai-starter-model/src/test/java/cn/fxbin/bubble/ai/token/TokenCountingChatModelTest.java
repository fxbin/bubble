package cn.fxbin.bubble.ai.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TokenCountingChatModel Tests
 *
 * @author fxbin
 */
@ExtendWith(MockitoExtension.class)
class TokenCountingChatModelTest {

    @Mock
    private ChatModel delegate;

    @Mock
    private TokenUsageRecorder recorder;

    @Mock
    private TokenCountEstimator tokenCountEstimator;

    @Test
    void testCallRecordsUsage() {
        // Setup
        TokenCountingChatModel model = new TokenCountingChatModel(delegate, recorder, tokenCountEstimator, "openai", "gpt-4");
        
        ChatResponse response = mock(ChatResponse.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);
        
        when(delegate.call(any(Prompt.class))).thenReturn(response);
        when(response.getMetadata()).thenReturn(metadata);
        when(metadata.getUsage()).thenReturn(usage);
        when(usage.getTotalTokens()).thenReturn(1);
        
        // Act
        model.call(new Prompt("hello"));
        
        // Assert
        verify(recorder, timeout(1000).times(1)).record(any(TokenUsageContext.class));
    }

    @Test
    void testCallEstimatesUsageWhenProviderUsageIsInvalid() {
        TokenCountingChatModel model = new TokenCountingChatModel(delegate, recorder, tokenCountEstimator, "openai", "gpt-4");

        ChatResponse response = mock(ChatResponse.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);

        Generation result = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);
        when(output.getText()).thenReturn("answer");
        when(result.getOutput()).thenReturn(output);

        when(delegate.call(any(Prompt.class))).thenReturn(response);
        when(response.getMetadata()).thenReturn(metadata);
        when(metadata.getUsage()).thenReturn(usage);
        when(usage.getTotalTokens()).thenReturn(0);
        when(response.getResult()).thenReturn(result);

        when(tokenCountEstimator.estimate(eq("sys\nhi"))).thenReturn(3);
        when(tokenCountEstimator.estimate(eq("answer"))).thenReturn(5);

        Prompt prompt = new Prompt(List.of(new SystemMessage("sys"), new UserMessage("hi")));

        model.call(prompt);

        verify(tokenCountEstimator, times(1)).estimate(eq("sys\nhi"));
        verify(tokenCountEstimator, times(1)).estimate(eq("answer"));

        var captor = org.mockito.ArgumentCaptor.forClass(TokenUsageContext.class);
        verify(recorder, timeout(1000).times(1)).record(captor.capture());
        TokenUsageContext context = captor.getValue();

        assertThat(context.platform()).isEqualTo("openai");
        assertThat(context.model()).isEqualTo("gpt-4");
        assertThat(context.usage().getPromptTokens()).isEqualTo(3);
        assertThat(context.usage().getCompletionTokens()).isEqualTo(5);
    }

    @Test
    void testCallBuildsPromptTextWithFallbackWhenMessageGetTextThrows() {
        TokenCountingChatModel model = new TokenCountingChatModel(delegate, recorder, tokenCountEstimator, "openai", "gpt-4");

        ChatResponse response = mock(ChatResponse.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);

        Generation result = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);
        when(output.getText()).thenReturn("ok");
        when(result.getOutput()).thenReturn(output);

        Message broken = mock(Message.class);
        when(broken.getText()).thenThrow(new RuntimeException("boom"));
        when(broken.toString()).thenReturn("BROKEN_MESSAGE");

        when(delegate.call(any(Prompt.class))).thenReturn(response);
        when(response.getMetadata()).thenReturn(metadata);
        when(metadata.getUsage()).thenReturn(usage);
        when(usage.getTotalTokens()).thenReturn(0);
        when(response.getResult()).thenReturn(result);

        when(tokenCountEstimator.estimate(eq("BROKEN_MESSAGE"))).thenReturn(1);
        when(tokenCountEstimator.estimate(eq("ok"))).thenReturn(1);

        Prompt prompt = new Prompt(List.of(broken));

        model.call(prompt);

        verify(tokenCountEstimator, times(1)).estimate(eq("BROKEN_MESSAGE"));
        verify(tokenCountEstimator, times(1)).estimate(eq("ok"));
    }

    @Test
    void testStream_UsesProviderUsageWhenPresent() {
        TokenCountingChatModel model = new TokenCountingChatModel(delegate, recorder, tokenCountEstimator, "openai", "gpt-4");

        ChatResponse r1 = mock(ChatResponse.class);
        ChatResponseMetadata m1 = mock(ChatResponseMetadata.class);
        Usage u1 = mock(Usage.class);
        
        when(r1.getMetadata()).thenReturn(m1);
        when(m1.getUsage()).thenReturn(u1);
        when(u1.getTotalTokens()).thenReturn(10);

        when(delegate.stream(any(Prompt.class))).thenReturn(Flux.just(r1));

        model.stream(new Prompt(List.of(new UserMessage("hi")))).collectList().block();

        verify(recorder, timeout(1000).times(1)).record(any(TokenUsageContext.class));
        verify(tokenCountEstimator, never()).estimate(any(String.class));
    }

    @Test
    void testStream_EstimatesUsageWhenProviderUsageMissing() {
        TokenCountingChatModel model = new TokenCountingChatModel(delegate, recorder, tokenCountEstimator, "openai", "gpt-4", true);

        ChatResponse r1 = mock(ChatResponse.class);
        Generation g1 = mock(Generation.class);
        AssistantMessage o1 = mock(AssistantMessage.class);
        when(o1.getText()).thenReturn("a".repeat(500));
        when(g1.getOutput()).thenReturn(o1);
        when(r1.getResult()).thenReturn(g1);
        when(r1.getMetadata()).thenReturn(null);

        ChatResponse r2 = mock(ChatResponse.class);
        Generation g2 = mock(Generation.class);
        AssistantMessage o2 = mock(AssistantMessage.class);
        when(o2.getText()).thenReturn("b".repeat(500));
        when(g2.getOutput()).thenReturn(o2);
        when(r2.getResult()).thenReturn(g2);
        when(r2.getMetadata()).thenReturn(null);

        when(delegate.stream(any(Prompt.class))).thenReturn(Flux.just(r1, r2));

        when(tokenCountEstimator.estimate(eq("hi"))).thenReturn(1);
        when(tokenCountEstimator.estimate(eq("a".repeat(500) + "b".repeat(500)))).thenReturn(1000);

        model.stream(new Prompt(List.of(new UserMessage("hi")))).collectList().block();

        verify(tokenCountEstimator, times(1)).estimate(eq("hi"));
        verify(tokenCountEstimator, times(1)).estimate(eq("a".repeat(500) + "b".repeat(500)));
        verify(recorder, timeout(1000).times(1)).record(any(TokenUsageContext.class));
    }
}
