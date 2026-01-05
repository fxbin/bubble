package cn.fxbin.bubble.ai.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BubbleAiPropertiesTest {

    @Test
    void defaultsShouldBeCorrect() {
        BubbleAiProperties properties = new BubbleAiProperties();

        assertThat(properties.getTokenCounting().isEnabled()).isTrue();
    }
}

