package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import cn.fxbin.bubble.ai.mapper.AiModelGroupMapper;
import cn.fxbin.bubble.ai.service.AiModelGroupService;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * AI model group service implementation.
 *
 * @author fxbin
 */
@RequiredArgsConstructor
public class AiModelGroupServiceImpl extends ServiceImpl<AiModelGroupMapper, AiModelGroup> implements AiModelGroupService {

    private final AiModelGroupMapper aiModelGroupMapper;

    @Override
    public AiModelGroup getEnabledGroup(String identifier) {
        if (StrUtil.isBlank(identifier)) {
            return null;
        }
        Long resolvedId = parseLongOrNull(identifier);
        if (resolvedId != null) {
            AiModelGroup group = aiModelGroupMapper.selectOne(Wrappers.lambdaQuery(AiModelGroup.class)
                    .eq(AiModelGroup::getEnabled, true)
                    .eq(AiModelGroup::getId, resolvedId));
            if (group != null) {
                return group;
            }
        }
        return aiModelGroupMapper.selectOne(Wrappers.lambdaQuery(AiModelGroup.class)
                .eq(AiModelGroup::getEnabled, true)
                .eq(AiModelGroup::getGroupCode, identifier));
    }

    @Override
    public List<AiModelGroup> listEnabledGroups() {
        return aiModelGroupMapper.selectList(Wrappers.lambdaQuery(AiModelGroup.class)
                .eq(AiModelGroup::getEnabled, true)
                .orderByAsc(AiModelGroup::getId));
    }

    @Override
    public void validateGroupCodeUnique(String groupCode, Long id) {
        long count = count(Wrappers.lambdaQuery(AiModelGroup.class)
                .eq(AiModelGroup::getGroupCode, groupCode)
                .ne(id != null, AiModelGroup::getId, id));
        if (count > 0) {
            throw new ServiceException("模型组编码 " + groupCode + " 已存在");
        }
    }

    @Override
    public void createGroup(AiModelGroup aiModelGroup) {
        validateGroupCodeUnique(aiModelGroup.getGroupCode(), null);
        save(aiModelGroup);
    }

    @Override
    public void updateGroup(AiModelGroup aiModelGroup) {
        validateGroupCodeUnique(aiModelGroup.getGroupCode(), aiModelGroup.getId());
        updateById(aiModelGroup);
    }

    @Override
    public void removeGroup(Long id) {
        removeById(id);
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
