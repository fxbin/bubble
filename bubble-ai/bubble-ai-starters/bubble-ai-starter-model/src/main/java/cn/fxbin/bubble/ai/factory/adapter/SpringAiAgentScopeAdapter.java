package cn.fxbin.bubble.ai.factory.adapter;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.ModelUtils;
import io.agentscope.core.model.ToolSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Spring AI 到 AgentScope 的适配器
 *
 * <p>将 Spring AI 的 ChatModel 适配为 AgentScope 的 ChatModelBase。
 *
 * @author fxbin
 * @since 2026-01-15 18:15:00.000
 */
@Slf4j
public class SpringAiAgentScopeAdapter extends ChatModelBase {

    /**
     * Spring AI 聊天模型实例
     */
    private final org.springframework.ai.chat.model.ChatModel springAiChatModel;

    /**
     * 模型名称
     */
    private final String modelName;

    /**
     * 构造函数
     *
     * @param springAiChatModel Spring AI 聊天模型实例
     */
    public SpringAiAgentScopeAdapter(org.springframework.ai.chat.model.ChatModel springAiChatModel) {
        this(springAiChatModel, "SpringAiAdapterModel");
    }

    /**
     * 构造函数
     *
     * @param springAiChatModel Spring AI 聊天模型实例
     * @param modelName         模型名称
     */
    public SpringAiAgentScopeAdapter(org.springframework.ai.chat.model.ChatModel springAiChatModel, String modelName) {
        this.springAiChatModel = springAiChatModel;
        this.modelName = modelName;
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    /**
     * 执行流式对话请求
     *
     * @param messages 消息列表
     * @param tools    工具列表（暂未支持）
     * @param options  生成选项
     * @return 聊天响应流
     */
    @Override
    protected Flux<ChatResponse> doStream(List<Msg> messages, List<ToolSchema> tools, GenerateOptions options) {
        // 1. 记录日志
        log.debug("SpringAI Adapter API call: model={}", getModelName());

        // 2. 如果存在工具，记录警告（暂未支持工具调用映射）
        if (tools != null && !tools.isEmpty()) {
            log.warn("Tools are provided but not supported in SpringAiAgentScopeAdapter yet.");
        }

        // 3. 构建响应流
        Flux<ChatResponse> responseFlux = Flux.defer(() -> {
            // 3.1 将 AgentScope Msg 转换为 Spring AI Message
            List<org.springframework.ai.chat.messages.Message> springMessages = messages.stream()
                    .map(this::toSpringMessage)
                    .collect(Collectors.toList());

            // 3.2 构建 Prompt
            // TODO: 暂时无法将 AgentScope 的 GenerateOptions (如 temperature, topP) 自动映射到 Spring AI 的 Prompt Options。
            // 因为 Spring AI 的 ChatOptions 通常与具体实现绑定 (如 OpenAiChatOptions)，而此处是通用适配器。
            // 如果需要支持参数透传，需要根据底层模型类型进行 instanceof 判断并构建对应的 Options，或者等待 Spring AI 提供通用的 PortableChatOptions。
            Prompt prompt = new Prompt(springMessages);

            // 3.3 调用 Spring AI 模型 (流式)
            return springAiChatModel.stream(prompt)
                    .map(this::toAgentScopeResponse);
        }).subscribeOn(Schedulers.boundedElastic());

        // 4. 应用超时和重试逻辑 (参考 AgentScope 原生实现)
        return ModelUtils.applyTimeoutAndRetry(
                responseFlux,
                options,
                null, // defaultOptions 暂未传入，可视情况添加
                getModelName(),
                "spring-ai"
        );
    }

    /**
     * 将 AgentScope Msg 转换为 Spring AI Message
     *
     * @param msg AgentScope 消息
     * @return Spring AI 消息
     */
    private org.springframework.ai.chat.messages.Message toSpringMessage(Msg msg) {
        MsgRole role = msg.getRole();
        // 提取文本内容
        String content = msg.getContent().stream()
                .map(block -> {
                    if (block instanceof TextBlock) {
                        return ((TextBlock) block).getText();
                    }
                    return ""; // 暂时忽略其他类型的块
                })
                .collect(Collectors.joining("\n"));

        // 根据角色创建对应的 Spring AI 消息
        if (MsgRole.USER.equals(role)) {
            return new UserMessage(content);
        } else if (MsgRole.SYSTEM.equals(role)) {
            return new SystemMessage(content);
        } else if (MsgRole.ASSISTANT.equals(role)) {
            return new AssistantMessage(content);
        } else {
            // 未知角色或工具角色默认作为用户消息处理
            return new UserMessage(content);
        }
    }

    /**
     * 将 Spring AI ChatResponse 转换为 AgentScope ChatResponse
     *
     * @param springResponse Spring AI 响应
     * @return AgentScope 响应
     */
    private ChatResponse toAgentScopeResponse(org.springframework.ai.chat.model.ChatResponse springResponse) {
        // 获取响应文本
        String contentText = springResponse.getResult().getOutput().getText();

        // 构建内容块
        List<ContentBlock> contentBlocks = Collections.singletonList(
                TextBlock.builder().text(contentText).build()
        );

        // 构建 Usage 信息
        ChatUsage usage = Optional.ofNullable(springResponse.getMetadata().getUsage())
                .map(u -> new ChatUsage(
                        Optional.ofNullable(u.getPromptTokens()).orElse(0),
                        Optional.ofNullable(u.getCompletionTokens()).orElse(0),
                        0.0 // 时间信息 Spring AI 未直接提供标准化的 double 类型，暂设为 0
                ))
                .orElse(new ChatUsage(0, 0, 0.0));

        // 构建元数据
        Map<String, Object> metadata = Collections.emptyMap(); // 可根据需要提取更多元数据

        String finishReason = null;
        // Spring AI 的 finish reason 通常在 metadata 中，这里暂不提取

        return new ChatResponse(
                UUID.randomUUID().toString(), // 生成唯一 ID
                contentBlocks,
                usage,
                metadata,
                finishReason
        );
    }
}
