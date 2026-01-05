package cn.fxbin.bubble.ai.manager;

import cn.fxbin.bubble.ai.domain.dto.AiModelInfo;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

/**
 * AI 模型管理器
 * <p>负责统一管理来自配置文件和数据库的模型</p>
 *
 * @author fxbin
 */
public interface AiModelManager {

    /**
     * 获取所有可用模型列表
     *
     * @return 模型列表
     */
    List<AiModelInfo> listAvailableModels();

    /**
     * 根据 ID 获取 ChatModel
     *
     * @param modelId 模型ID (对应配置文件Key或数据库配置名称)
     * @return ChatModel
     */
    ChatModel getChatModel(String modelId);

}
