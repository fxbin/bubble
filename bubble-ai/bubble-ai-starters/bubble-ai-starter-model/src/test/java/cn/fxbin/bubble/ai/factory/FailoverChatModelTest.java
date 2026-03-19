package cn.fxbin.bubble.ai.factory;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FailoverChatModelTest {

    @Test
    void call_ShouldFallbackToNextCandidate_WhenErrorIsRetryable() {
        ChatModel primary = mock(ChatModel.class);
        ChatModel secondary = mock(ChatModel.class);
        Prompt prompt = mock(Prompt.class);
        ChatResponse response = mock(ChatResponse.class);

        when(primary.call(prompt)).thenThrow(new IllegalStateException("upstream timeout", new SocketTimeoutException("timeout")));
        when(secondary.call(prompt)).thenReturn(response);

        FailoverChatModel model = new FailoverChatModel("test-group", List.of(
                new FailoverChatModel.Candidate("primary", primary),
                new FailoverChatModel.Candidate("secondary", secondary)
        ));

        ChatResponse actual = model.call(prompt);

        assertThat(actual).isSameAs(response);
        verify(primary).call(prompt);
        verify(secondary).call(prompt);
    }

    @Test
    void call_ShouldNotFallback_WhenErrorIsNotRetryable() {
        ChatModel primary = mock(ChatModel.class);
        ChatModel secondary = mock(ChatModel.class);
        Prompt prompt = mock(Prompt.class);

        when(primary.call(prompt)).thenThrow(new IllegalStateException("401 unauthorized"));

        FailoverChatModel model = new FailoverChatModel("test-group", List.of(
                new FailoverChatModel.Candidate("primary", primary),
                new FailoverChatModel.Candidate("secondary", secondary)
        ));

        assertThatThrownBy(() -> model.call(prompt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("401");

        verify(primary).call(prompt);
        verifyNoInteractions(secondary);
    }

    @Test
    void call_ShouldSkipCoolingDownCandidate_OnNextInvocation() {
        ChatModel primary = mock(ChatModel.class);
        ChatModel secondary = mock(ChatModel.class);
        Prompt prompt = mock(Prompt.class);
        ChatResponse response = mock(ChatResponse.class);
        AtomicLong clock = new AtomicLong(1_000L);

        when(primary.call(prompt)).thenThrow(new IllegalStateException("upstream timeout", new SocketTimeoutException("timeout")));
        when(secondary.call(prompt)).thenReturn(response);

        FailoverChatModel model = new FailoverChatModel("test-group-cooldown", List.of(
                new FailoverChatModel.Candidate("primary", primary),
                new FailoverChatModel.Candidate("secondary", secondary)
        ), 30_000L, clock::get);

        ChatResponse first = model.call(prompt);
        ChatResponse second = model.call(prompt);

        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        verify(primary, times(1)).call(prompt);
        verify(secondary, times(2)).call(prompt);
    }

    @Test
    void call_ShouldRetryCandidate_AfterCooldownExpires() {
        ChatModel primary = mock(ChatModel.class);
        ChatModel secondary = mock(ChatModel.class);
        Prompt prompt = mock(Prompt.class);
        ChatResponse response = mock(ChatResponse.class);
        AtomicLong clock = new AtomicLong(1_000L);

        when(primary.call(prompt))
                .thenThrow(new IllegalStateException("upstream timeout", new SocketTimeoutException("timeout")))
                .thenReturn(response);
        when(secondary.call(prompt)).thenReturn(response);

        FailoverChatModel model = new FailoverChatModel("test-group-expire", List.of(
                new FailoverChatModel.Candidate("primary", primary),
                new FailoverChatModel.Candidate("secondary", secondary)
        ), 30_000L, clock::get);

        ChatResponse first = model.call(prompt);
        clock.addAndGet(31_000L);
        ChatResponse second = model.call(prompt);

        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        verify(primary, times(2)).call(prompt);
    }
}
