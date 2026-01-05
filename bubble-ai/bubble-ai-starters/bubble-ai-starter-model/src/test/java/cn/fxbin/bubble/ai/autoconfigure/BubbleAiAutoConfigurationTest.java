package cn.fxbin.bubble.ai.autoconfigure;

import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import cn.fxbin.bubble.ai.service.AiModelConfigService;
import cn.fxbin.bubble.ai.token.TokenUsageRecorder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.FilteredClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

class BubbleAiAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BubbleAiAutoConfiguration.class))
            .withBean(AiModelConfigMapper.class, () -> Mockito.mock(AiModelConfigMapper.class));

    @Test
    void shouldCreateCoreBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TokenUsageRecorder.class);
            assertThat(context).hasSingleBean(TokenCountEstimator.class);
            assertThat(context).hasSingleBean(AiModelFactory.class);
        });
    }

    @Test
    void shouldNotCreateAiModelConfigServiceWhenIServiceMissing() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("com.baomidou.mybatisplus.extension.service.IService"))
                .withConfiguration(AutoConfigurations.of(BubbleAiAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(AiModelConfigService.class));
    }

    @Test
    void shouldCreateAiModelConfigServiceWhenIServicePresent() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(AiModelConfigService.class));
    }

}
