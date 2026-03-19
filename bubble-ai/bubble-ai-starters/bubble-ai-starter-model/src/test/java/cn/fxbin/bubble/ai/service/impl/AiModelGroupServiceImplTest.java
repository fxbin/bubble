package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import cn.fxbin.bubble.ai.mapper.AiModelGroupMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiModelGroupServiceImplTest {

    @Mock
    private AiModelGroupMapper groupMapper;

    @Test
    void getEnabledGroup_ShouldResolveByCode() {
        AiModelGroup group = createGroup(1L, "chat-group");
        when(groupMapper.selectOne(any())).thenReturn(group);

        AiModelGroupServiceImpl service = new AiModelGroupServiceImpl(groupMapper);

        AiModelGroup result = service.getEnabledGroup("chat-group");

        assertThat(result).isSameAs(group);
    }

    @Test
    void listEnabledGroups_ShouldDelegateToMapper() {
        AiModelGroup group = createGroup(1L, "chat-group");
        when(groupMapper.selectList(any())).thenReturn(List.of(group));

        AiModelGroupServiceImpl service = new AiModelGroupServiceImpl(groupMapper);

        assertThat(service.listEnabledGroups()).containsExactly(group);
    }

    private AiModelGroup createGroup(Long id, String groupCode) {
        AiModelGroup group = new AiModelGroup();
        group.setId(id);
        group.setGroupCode(groupCode);
        group.setGroupName("Group " + groupCode);
        group.setEnabled(true);
        return group;
    }
}
