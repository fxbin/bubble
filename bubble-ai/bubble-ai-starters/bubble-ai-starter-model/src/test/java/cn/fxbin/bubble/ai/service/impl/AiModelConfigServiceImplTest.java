package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.factory.FailoverChatModel;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import cn.fxbin.bubble.ai.mapper.AiModelGroupMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

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
    private AiModelGroupMapper groupMapper;

    @Mock
    private ChatModel chatModel;

    @Test
    void getChatModel_WhenConfigNameBlank_ShouldThrow() {
        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper, groupMapper);

        assertThatThrownBy(() -> service.getChatModel("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getChatModel_ShouldDelegateToFactoryWithTopK() {
        AiModelConfig config = createConfig(1L, "c1", "m", null, false, 100);
        config.setTopK(50);

        when(mapper.selectOne(any())).thenReturn(config);
        when(aiModelFactory.getOrCreateChatModel(eq(AiPlatformEnum.OPENAI), eq("k"), eq("u"), eq("m"), eq(0.3), eq(50), eq(null)))
                .thenReturn(chatModel);

        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper, groupMapper);

        ChatModel result = service.getChatModel("c1");

        assertThat(result).isSameAs(chatModel);
    }

    @Test
    void getChatModel_WithConfigGroupFallback_ShouldReturnFailoverModel() {
        AiModelConfig primary = createConfig(1L, "primary", "m", "group-a", true, 1);
        AiModelConfig secondary = createConfig(2L, "secondary", "m", "group-a", true, 2);
        AiModelGroup group = createGroup(10L, "group-a");

        when(mapper.selectOne(any())).thenReturn(primary);
        when(mapper.selectList(any())).thenReturn(List.of(primary, secondary));
        when(groupMapper.selectOne(any())).thenReturn(group);
        when(aiModelFactory.getOrCreateChatModel(eq(AiPlatformEnum.OPENAI), eq("k"), eq("u"), eq("m"), eq(0.3), eq(10), eq(null)))
                .thenReturn(chatModel);

        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper, groupMapper);

        ChatModel result = service.getChatModel("primary");

        assertThat(result).isInstanceOf(FailoverChatModel.class);
    }

    @Test
    void getChatModel_WithGroupId_ShouldReturnFailoverModel() {
        AiModelConfig primary = createConfig(1L, "primary", "m", "group-a", true, 1);
        AiModelConfig secondary = createConfig(2L, "secondary", "m", "group-a", true, 2);
        AiModelGroup group = createGroup(10L, "group-a");

        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.selectList(any())).thenReturn(List.of(primary, secondary));
        when(groupMapper.selectOne(any())).thenReturn(group);
        when(aiModelFactory.getOrCreateChatModel(eq(AiPlatformEnum.OPENAI), eq("k"), eq("u"), eq("m"), eq(0.3), eq(10), eq(null)))
                .thenReturn(chatModel);

        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper, groupMapper);

        ChatModel result = service.getChatModel("group-a");

        assertThat(result).isInstanceOf(FailoverChatModel.class);
    }

    @Test
    void getChatModel_WithNumericGroupId_ShouldResolveGroupEntity() {
        AiModelConfig primary = createConfig(1L, "primary", "m", "group-a", true, 1);
        AiModelConfig secondary = createConfig(2L, "secondary", "m", "group-a", true, 2);
        AiModelGroup group = createGroup(10L, "group-a");

        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.selectList(any())).thenReturn(List.of(primary, secondary));
        when(groupMapper.selectOne(any())).thenReturn(group);
        when(aiModelFactory.getOrCreateChatModel(eq(AiPlatformEnum.OPENAI), eq("k"), eq("u"), eq("m"), eq(0.3), eq(10), eq(null)))
                .thenReturn(chatModel);

        AiModelConfigServiceImpl service = new AiModelConfigServiceImpl(aiModelFactory, mapper, groupMapper);

        ChatModel result = service.getChatModel("10");

        assertThat(result).isInstanceOf(FailoverChatModel.class);
    }

    private AiModelConfig createConfig(Long id, String configName, String model, String groupId, boolean fallback, int priority) {
        AiModelConfig config = new AiModelConfig();
        config.setId(id);
        config.setConfigName(configName);
        config.setEnabled(true);
        config.setPlatform(AiPlatformEnum.OPENAI);
        config.setApiKey("k");
        config.setBaseUrl("u");
        config.setModel(model);
        config.setTemperature(0.3);
        config.setTopK(10);
        config.setGroupId(groupId);
        config.setFallbackToGroupEnabled(fallback);
        config.setPriority(priority);
        return config;
    }

    private AiModelGroup createGroup(Long id, String groupCode) {
        AiModelGroup group = new AiModelGroup();
        group.setId(id);
        group.setGroupCode(groupCode);
        group.setGroupName("Group " + groupCode);
        group.setEnabled(true);
        group.setFailoverEnabled(true);
        group.setCooldownSeconds(30);
        return group;
    }
}
