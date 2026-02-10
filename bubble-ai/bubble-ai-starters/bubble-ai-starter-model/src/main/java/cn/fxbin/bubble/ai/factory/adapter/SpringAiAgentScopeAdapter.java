package cn.fxbin.bubble.ai.factory.adapter;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.ImageBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.message.URLSource;
import io.agentscope.core.message.Base64Source;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    private static final String DEFAULT_MODEL_NAME = "SpringAiAdapterModel";
    private static final String PROVIDER_ID = "spring-ai";
    private static final String TOOL_USE_TAG = "tool_use";
    private static final String TOOL_RESULT_TAG = "tool_result";
    private static final String TOOL_DEFAULT_NAME = "tool";
    private static final String TOOL_DEFAULT_ID = "call";
    private static final String TOOL_HINT_PREFIX = "tools";
    private static final String LINE_SEPARATOR = "\n";
    private static final String MODEL_CLASS_OPENAI = "org.springframework.ai.openai.OpenAiChatModel";
    private static final String OPTIONS_CLASS_OPENAI = "org.springframework.ai.openai.OpenAiChatOptions";
    private static final String MODEL_CLASS_ZHIPU = "org.springframework.ai.zhipuai.ZhiPuAiChatModel";
    private static final String OPTIONS_CLASS_ZHIPU = "org.springframework.ai.zhipuai.ZhiPuAiChatOptions";
    private static final String MODEL_CLASS_MINIMAX = "org.springframework.ai.minimax.MiniMaxChatModel";
    private static final String OPTIONS_CLASS_MINIMAX = "org.springframework.ai.minimax.MiniMaxChatOptions";
    private static final String MODEL_CLASS_DEEPSEEK = "org.springframework.ai.deepseek.DeepSeekChatModel";
    private static final String OPTIONS_CLASS_DEEPSEEK = "org.springframework.ai.deepseek.DeepSeekChatOptions";
    private static final String MODEL_CLASS_ANTHROPIC = "org.springframework.ai.anthropic.AnthropicChatModel";
    private static final String OPTIONS_CLASS_ANTHROPIC = "org.springframework.ai.anthropic.AnthropicChatOptions";
    private static final String MODEL_CLASS_OLLAMA = "org.springframework.ai.ollama.OllamaChatModel";
    private static final String OPTIONS_CLASS_OLLAMA = "org.springframework.ai.ollama.OllamaChatOptions";
    private static final String METHOD_GET_FINISH_REASON = "getFinishReason";
    private static final String METHOD_GET_USAGE = "getUsage";
    private static final String METHOD_GET_ADDITIONAL_METADATA = "getAdditionalMetadata";
    private static final String METHOD_GET_ADDITIONAL_PROPERTIES = "getAdditionalProperties";
    private static final String METADATA_FINISH_REASON = "finishReason";
    private static final String METHOD_GET_PROMPT_TOKENS = "getPromptTokens";
    private static final String METHOD_GET_COMPLETION_TOKENS = "getCompletionTokens";

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
        this(springAiChatModel, DEFAULT_MODEL_NAME);
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
            Prompt prompt = buildPrompt(springMessages, tools, options);

            // 3.3 调用 Spring AI 模型 (流式)
            return springAiChatModel.stream(prompt)
                    .map(this::toAgentScopeResponse);
        }).subscribeOn(Schedulers.boundedElastic());

        // 4. 应用超时和重试逻辑
        return ModelUtils.applyTimeoutAndRetry(
                responseFlux,
                options,
                null, // defaultOptions 暂未传入，可视情况添加
                getModelName(),
                PROVIDER_ID
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
                .map(this::toPlainText)
                .filter(s -> s != null && !s.isEmpty())
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

    private String toPlainText(ContentBlock block) {
        if (block instanceof TextBlock) {
            return ((TextBlock) block).getText();
        }
        if (block instanceof ImageBlock) {
            ImageBlock ib = (ImageBlock) block;
            if (ib.getSource() instanceof URLSource url) {
                return "![image](" + url.getUrl() + ")";
            }
            if (ib.getSource() instanceof Base64Source b64) {
                return "![image](data:" + b64.getMediaType() + ";base64," + b64.getData() + ")";
            }
            return "[image]";
        }
        if (block instanceof ToolUseBlock) {
            ToolUseBlock tu = (ToolUseBlock) block;
            String name = tu.getName() != null ? tu.getName() : TOOL_DEFAULT_NAME;
            String id = tu.getId() != null ? tu.getId() : TOOL_DEFAULT_ID;
            String inputJson = tu.getInput() != null ? tu.getInput().toString() : "{}";
            return "<" + TOOL_USE_TAG + " name=\"" + name + "\" id=\"" + id + "\">" + inputJson + "</" + TOOL_USE_TAG + ">";
        }
        if (block instanceof ToolResultBlock) {
            ToolResultBlock tr = (ToolResultBlock) block;
            String id = tr.getId() != null ? tr.getId() : TOOL_DEFAULT_ID;
            String name = tr.getName() != null ? tr.getName() : TOOL_DEFAULT_NAME;
            String out = tr.getOutput() != null ? tr.getOutput().stream().map(this::toPlainText).filter(s -> s != null).collect(Collectors.joining(LINE_SEPARATOR)) : "";
            return "<" + TOOL_RESULT_TAG + " name=\"" + name + "\" id=\"" + id + "\">" + out + "</" + TOOL_RESULT_TAG + ">";
        }
        return "";
    }

    private Prompt buildPrompt(List<org.springframework.ai.chat.messages.Message> springMessages, List<ToolSchema> tools, GenerateOptions options) {
        List<org.springframework.ai.chat.messages.Message> preparedMessages = new ArrayList<>(springMessages);
        String toolHint = buildToolHint(tools);
        if (!toolHint.isEmpty()) {
            preparedMessages.add(0, new SystemMessage(toolHint));
        }
        Object chatOptions = buildChatOptions(options);
        return createPrompt(preparedMessages, chatOptions);
    }

    private String buildToolHint(List<ToolSchema> tools) {
        if (tools == null || tools.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(TOOL_HINT_PREFIX);
        for (ToolSchema tool : tools) {
            builder.append(LINE_SEPARATOR)
                    .append("- ")
                    .append(tool.getName())
                    .append(": ")
                    .append(Optional.ofNullable(tool.getDescription()).orElse(""))
                    .append(" ")
                    .append(Optional.ofNullable(tool.getParameters()).orElse(Collections.emptyMap()));
        }
        return builder.toString();
    }

    private Object buildChatOptions(GenerateOptions options) {
        if (options == null) {
            return null;
        }
        String optionsClassName = resolveOptionsClassName();
        if (optionsClassName == null) {
            return null;
        }
        Object builder = invokeStatic(optionsClassName, "builder");
        if (builder == null) {
            return null;
        }
        applyIfNotNull(builder, "model", options.getModelName());
        applyIfNotNull(builder, "temperature", options.getTemperature());
        applyIfNotNull(builder, "topP", options.getTopP());
        applyIfNotNull(builder, "topK", options.getTopK());
        applyIfNotNull(builder, "maxTokens", options.getMaxTokens());
        applyIfNotNull(builder, "frequencyPenalty", options.getFrequencyPenalty());
        applyIfNotNull(builder, "presencePenalty", options.getPresencePenalty());
        applyIfNotNull(builder, "seed", options.getSeed());
        return invoke(builder, "build");
    }

    private String resolveOptionsClassName() {
        String modelClassName = springAiChatModel.getClass().getName();
        if (MODEL_CLASS_OPENAI.equals(modelClassName)) {
            return OPTIONS_CLASS_OPENAI;
        }
        if (MODEL_CLASS_ZHIPU.equals(modelClassName)) {
            return OPTIONS_CLASS_ZHIPU;
        }
        if (MODEL_CLASS_MINIMAX.equals(modelClassName)) {
            return OPTIONS_CLASS_MINIMAX;
        }
        if (MODEL_CLASS_DEEPSEEK.equals(modelClassName)) {
            return OPTIONS_CLASS_DEEPSEEK;
        }
        if (MODEL_CLASS_ANTHROPIC.equals(modelClassName)) {
            return OPTIONS_CLASS_ANTHROPIC;
        }
        if (MODEL_CLASS_OLLAMA.equals(modelClassName)) {
            return OPTIONS_CLASS_OLLAMA;
        }
        return null;
    }

    private Prompt createPrompt(List<org.springframework.ai.chat.messages.Message> messages, Object chatOptions) {
        if (chatOptions == null) {
            return new Prompt(messages);
        }
        try {
            for (Constructor<?> constructor : Prompt.class.getConstructors()) {
                if (constructor.getParameterCount() != 2) {
                    continue;
                }
                Class<?> firstParam = constructor.getParameterTypes()[0];
                Class<?> secondParam = constructor.getParameterTypes()[1];
                if (!firstParam.isAssignableFrom(List.class)) {
                    continue;
                }
                if (!secondParam.isAssignableFrom(chatOptions.getClass())) {
                    continue;
                }
                return (Prompt) constructor.newInstance(messages, chatOptions);
            }
        } catch (Exception ignored) {
            return new Prompt(messages);
        }
        return new Prompt(messages);
    }

    private Object invokeStatic(String className, String methodName) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            return method.invoke(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyIfNotNull(Object target, String methodName, Object value) {
        if (value == null) {
            return;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            Class<?> paramType = method.getParameterTypes()[0];
            if (!isCompatible(paramType, value.getClass())) {
                continue;
            }
            try {
                method.invoke(target, value);
                return;
            } catch (Exception ignored) {
                return;
            }
        }
    }

    private boolean isCompatible(Class<?> paramType, Class<?> valueType) {
        if (paramType.isPrimitive()) {
            return wrapPrimitive(paramType).isAssignableFrom(valueType);
        }
        return paramType.isAssignableFrom(valueType);
    }

    private Class<?> wrapPrimitive(Class<?> primitive) {
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return primitive;
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

        ChatUsage usage = extractUsage(springResponse);
        Map<String, Object> metadata = extractMetadataMap(springResponse.getMetadata());
        String finishReason = extractFinishReason(springResponse.getMetadata());

        return new ChatResponse(
                UUID.randomUUID().toString(), // 生成唯一 ID
                contentBlocks,
                usage,
                metadata,
                finishReason
        );
    }

    private ChatUsage extractUsage(org.springframework.ai.chat.model.ChatResponse springResponse) {
        Object metadata = springResponse.getMetadata();
        Object usageObject = invokeIfExists(metadata, METHOD_GET_USAGE);
        if (usageObject == null) {
            return new ChatUsage(0, 0, 0.0);
        }
        try {
            Method promptTokens = usageObject.getClass().getMethod(METHOD_GET_PROMPT_TOKENS);
            Method completionTokens = usageObject.getClass().getMethod(METHOD_GET_COMPLETION_TOKENS);
            Integer prompt = (Integer) promptTokens.invoke(usageObject);
            Integer completion = (Integer) completionTokens.invoke(usageObject);
            return new ChatUsage(
                    Optional.ofNullable(prompt).orElse(0),
                    Optional.ofNullable(completion).orElse(0),
                    0.0
            );
        } catch (Exception ignored) {
            return new ChatUsage(0, 0, 0.0);
        }
    }

    private String extractFinishReason(Object metadata) {
        if (metadata == null) {
            return null;
        }
        Object value = invokeIfExists(metadata, METHOD_GET_FINISH_REASON);
        if (value instanceof String) {
            return (String) value;
        }
        if (metadata instanceof Map<?, ?> mapValue) {
            Object raw = mapValue.get(METADATA_FINISH_REASON);
            return raw instanceof String ? (String) raw : null;
        }
        return null;
    }

    private Map<String, Object> extractMetadataMap(Object metadata) {
        if (metadata == null) {
            return Collections.emptyMap();
        }
        if (metadata instanceof Map<?, ?> mapValue) {
            return toStringObjectMap(mapValue);
        }
        Object additional = invokeIfExists(metadata, METHOD_GET_ADDITIONAL_METADATA);
        if (additional instanceof Map<?, ?> additionalMap) {
            return toStringObjectMap(additionalMap);
        }
        Object properties = invokeIfExists(metadata, METHOD_GET_ADDITIONAL_PROPERTIES);
        if (properties instanceof Map<?, ?> propertiesMap) {
            return toStringObjectMap(propertiesMap);
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> toStringObjectMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() instanceof String key) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    private Object invokeIfExists(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }
}
