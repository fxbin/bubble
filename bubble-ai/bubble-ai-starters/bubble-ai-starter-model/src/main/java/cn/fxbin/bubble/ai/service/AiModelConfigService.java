package cn.fxbin.bubble.ai.service;

import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.ai.chat.model.ChatModel;

/**
 * AI 模型配置服务接口
 *
 * @author fxbin
 * @since 2026/01/05 21:00
 */
public interface AiModelConfigService extends IService<AiModelConfig> {

    /**
     * 根据配置ID获取 ChatModel
     *
     * @param modelId 模型ID (对应数据库配置ID)
     * @return {@link ChatModel}
     */
    ChatModel getChatModel(String modelId);

    /**
     * 校验配置名称是否唯一
     *
     * @param configName 配置名称
     * @param id         主键ID (排除自身)
     */
    void validateConfigNameUnique(String configName, Long id);

}
