package cn.fxbin.bubble.ai.lightrag.client;

import cn.fxbin.bubble.ai.lightrag.consts.ApiConst;
import cn.fxbin.bubble.ai.lightrag.model.ollama.*;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Headers;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;

/**
 * LightRAG Ollama 兼容接口客户端
 * 
 * <p>提供与 Ollama API 兼容的接口，使 LightRAG 可以作为 Ollama 服务器使用。
 * 支持版本查询、模型列表、运行状态等功能，便于与现有 Ollama 生态系统集成。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>版本信息查询</li>
 *   <li>可用模型列表</li>
 *   <li>运行中模型状态</li>
 *   <li>文本生成</li>
 *   <li>对话聊天</li>
 * </ul>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-22
 */
@Headers({
    ApiConst.Headers.ACCEPT_JSON
})
public interface LightRagOllamaClient extends LightRagBaseClient {

    /**
     * 获取版本信息
     * 
     * <p>获取 Ollama 兼容的版本信息。</p>
     * 
     * @return 版本信息
     */
    @Get("/api/version")
    VersionInfo getVersion();

    /**
     * 获取可用模型列表
     * 
     * <p>返回作为 Ollama 服务器可用的模型列表。</p>
     * 
     * @return 模型列表
     */
    @Get("/api/tags")
    ModelInfo getTags();

    /**
     * 获取运行中的模型
     * 
     * <p>列出当前运行中的模型。</p>
     * 
     * @return 运行中的模型列表
     */
    @Get("/api/ps")
    ModelInfo getRunningModels();

    /**
     * 文本生成
     * 
     * <p>处理生成完成请求，作为 Ollama 模型处理。
     * 出于兼容性目的，请求不由 LightRAG 处理，
     * 将由底层 LLM 模型处理。
     * 支持 application/json 和 application/octet-stream Content-Type。</p>
     * 
     * @param request 生成请求
     * @return 生成结果
     */
    @Post("/api/generate")
    OllamaGenerateResponse generate(@JSONBody OllamaGenerateRequest request);

    /**
     * 对话聊天
     * 
     * <p>处理聊天完成请求，作为 Ollama 模型处理。
     * 通过 LightRAG 路由用户查询，根据前缀指示器选择查询模式。
     * 检测并转发 OpenWebUI 会话相关请求（用于元数据生成任务）直接到 LLM。
     * 支持 application/json 和 application/octet-stream Content-Type。</p>
     * 
     * @param request 聊天请求
     * @return 聊天结果
     */
    @Post("/api/chat")
    OllamaChatResponse chat(@JSONBody OllamaChatRequest request);

}