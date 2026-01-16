package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiModelConfigServiceImplTest {

    @Mock
    private AiModelFactory aiModelFactory;

    @Mock
    private AiModelConfigMapper mapper;

    @Mock
    private ChatModel chatModel;

    @Test
    void getChatModel_WhenConfigNameBlank_ShouldThrow() {
        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper);

        assertThatThrownBy(() -> service.getChatModel("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getChatModel_ShouldDelegateToFactoryWithTopK() {
        AiModelConfig config = new AiModelConfig();
        config.setConfigName("c1");
        config.setEnabled(true);
        config.setPlatform(AiPlatformEnum.OPENAI);
        config.setApiKey("k");
        config.setBaseUrl("u");
        config.setModel("m");
        config.setTemperature(0.3);
        config.setTopK(50);

        when(mapper.selectOne(any())).thenReturn(config);
        when(aiModelFactory.getOrCreateChatModel(eq(AiPlatformEnum.OPENAI), eq("k"), eq("u"), eq("m"), eq(0.3), eq(50), eq(null)))
                .thenReturn(chatModel);

        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper);

        ChatModel result = service.getChatModel("c1");

        assertThat(result).isSameAs(chatModel);
    }
}

