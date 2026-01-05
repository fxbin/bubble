package cn.fxbin.bubble.ai.token;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.Usage;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultTokenUsageRecorderTest {

    @Test
    void record_WhenUsageIsNull_ShouldNotThrow() {
        DefaultTokenUsageRecorder recorder = new DefaultTokenUsageRecorder();

        assertThatCode(() -> recorder.record(new TokenUsageContext("openai", "m", null)))
                .doesNotThrowAnyException();
    }

    @Test
    void record_WhenUsagePresent_ShouldNotThrow() {
        DefaultTokenUsageRecorder recorder = new DefaultTokenUsageRecorder();

        Usage usage = mock(Usage.class);
        when(usage.toString()).thenReturn("u");

        assertThatCode(() -> recorder.record(new TokenUsageContext("openai", "m", usage)))
                .doesNotThrowAnyException();
    }
}

