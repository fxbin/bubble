package cn.fxbin.bubble.ai.service;

import cn.fxbin.bubble.ai.domain.dto.AiModelTestResult;
import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.ai.chat.model.ChatModel;

/**
 * AI 模型配置服务接口
 * <p>用于统一处理模型配置的创建、更新、删除、校验与测试等业务能力</p>
 * <p>面向控制层提供稳定的模型配置访问与校验入口</p>
 *
 * @author fxbin
 * @since 2026/01/05 21:00
 */
public interface AiModelConfigService extends IService<AiModelConfig> {

    /**
     * 根据配置ID获取 ChatModel
     * <p>用于在业务执行过程中按配置生成或获取可用的模型实例</p>
     *
     * @param modelId 模型配置ID，对应数据库主键
     * @return 可用的 ChatModel 实例
     */
    ChatModel getChatModel(String modelId);

    /**
     * 校验配置名称是否唯一
     * <p>用于创建或更新时保证配置名称不重复</p>
     *
     * @param configName 配置名称
     * @param id         主键ID，用于更新时排除自身
     */
    void validateConfigNameUnique(String configName, Long id);

    /**
     * 创建 AI 模型配置
     * <p>用于新增模型配置并完成必要的业务校验</p>
     *
     * @param aiModelConfig AI 模型配置实体
     */
    void createAiModel(AiModelConfig aiModelConfig);

    /**
     * 更新 AI 模型配置
     * <p>用于修改现有模型配置并保持配置一致性</p>
     *
     * @param aiModelConfig AI 模型配置实体
     */
    void updateAiModel(AiModelConfig aiModelConfig);

    /**
     * 删除 AI 模型配置
     * <p>用于移除指定模型配置并清理相关依赖</p>
     *
     * @param id 主键ID
     */
    void removeAiModel(Long id);

    /**
     * 检测指定模型配置是否可用
     * <p>用于在保存前或编辑时进行可用性验证，返回可用性与耗时信息</p>
     *
     * @param aiModelConfig 模型配置实体
     * @return 模型配置检测结果
     */
    AiModelTestResult testAiModel(AiModelConfig aiModelConfig);

    /**
     * 根据ID检测指定模型配置是否可用
     * <p>用于对已保存配置进行健康检测与故障排查</p>
     *
     * @param id 模型配置ID
     * @return 模型配置检测结果
     */
    AiModelTestResult testAiModelById(Long id);

}
