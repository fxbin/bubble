package cn.fxbin.bubble.ai.service;

import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * AI 模型分组服务接口
 * <p>用于统一处理模型分组的创建、更新、删除、查询与唯一性校验等业务能力</p>
 * <p>面向控制层提供稳定的模型分组访问入口，便于界面按“分组 -> 模型成员”方式进行管理</p>
 *
 * @author fxbin
 * @since 2026/03/19 16:47
 */
public interface AiModelGroupService extends IService<AiModelGroup> {

    /**
     * 根据分组标识获取已启用的模型分组
     * <p>支持使用分组主键 ID 或分组编码进行查询，主要用于运行时路由与界面详情加载</p>
     *
     * @param identifier 分组主键 ID 或分组编码
     * @return 已启用的模型分组；未找到时返回 {@code null}
     */
    AiModelGroup getEnabledGroup(String identifier);

    /**
     * 获取所有已启用的模型分组列表
     * <p>用于界面展示、分组选择器和运行时可用分组枚举</p>
     *
     * @return 已启用的模型分组列表
     */
    List<AiModelGroup> listEnabledGroups();

    /**
     * 校验分组编码是否唯一
     * <p>用于新增或更新时保证分组编码不重复，避免运行时路由歧义</p>
     *
     * @param groupCode 分组编码
     * @param id        主键 ID，用于更新时排除自身
     */
    void validateGroupCodeUnique(String groupCode, Long id);

    /**
     * 创建 AI 模型分组
     * <p>用于新增模型分组并完成必要的业务校验</p>
     *
     * @param aiModelGroup AI 模型分组实体
     */
    void createGroup(AiModelGroup aiModelGroup);

    /**
     * 更新 AI 模型分组
     * <p>用于修改已有模型分组并保持分组编码一致性</p>
     *
     * @param aiModelGroup AI 模型分组实体
     */
    void updateGroup(AiModelGroup aiModelGroup);

    /**
     * 删除 AI 模型分组
     * <p>用于移除指定模型分组；当前仅删除分组自身，不级联处理成员模型</p>
     *
     * @param id 主键 ID
     */
    void removeGroup(Long id);
}
