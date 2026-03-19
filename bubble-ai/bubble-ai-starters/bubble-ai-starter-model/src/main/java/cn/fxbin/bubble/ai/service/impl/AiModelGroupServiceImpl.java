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
 * AI 模型分组服务实现
 * <p>提供模型分组的查询、唯一性校验以及基础增删改业务实现</p>
 * <p>当前实现以分组编码为运行时主标识，同时兼容通过主键 ID 查询分组</p>
 *
 * @author fxbin
 * @since 2026/03/19 16:47
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

    /**
     * 将输入值解析为 Long 类型
     * <p>用于兼容“按主键 ID 或编码”两种分组查询方式</p>
     *
     * @param value 待解析的输入值
     * @return 解析成功返回 Long；无法解析时返回 {@code null}
     */
    private Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
