package cn.fxbin.bubble.flow.core.state.serializer;

import cn.fxbin.bubble.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * JSON-based implementation of {@link FlowStateSerializer}.
 *
 * @author fxbin
 * @since 2025/4/22
 */
@Slf4j
public class JsonFlowStateSerializer implements FlowStateSerializer {

    /**
     * 将上下文数据序列化为 JSON 字符串。
     *
     * @param contextData 包含上下文数据的 Map（例如变量、开始时间、版本）。
     * @return 上下文数据的 JSON 字符串表示。
     * @throws SerializationException 如果序列化过程中发生错误。
     */
    @Override
    public String serialize(Map<String, Object> contextData) throws SerializationException {
        try {
            return JsonUtils.toJson(contextData);
        } catch (Exception e) {
            log.error("Failed to serialize context data to JSON: {}", contextData, e);
            throw new SerializationException("Failed to serialize context data to JSON", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为上下文数据 Map。
     *
     * @param serializedContext JSON 字符串表示的上下文数据。
     * @return 反序列化后的上下文数据 Map。
     * @throws SerializationException 如果反序列化过程中发生错误或数据损坏。
     */
    @Override
    public Map<String, Object> deserialize(String serializedContext) throws SerializationException {
        try {
            if (serializedContext == null || serializedContext.isEmpty() || "null".equalsIgnoreCase(serializedContext.trim())) {
                log.warn("Attempted to deserialize null or empty string. Returning null.");
                // Or throw new SerializationException("Serialized context is null or empty");
                return null;
            }
            return JsonUtils.parse(serializedContext, Map.class);
        } catch (Exception e) {
            log.error("Failed to deserialize context data from JSON: {}", serializedContext, e);
            throw new SerializationException("Failed to deserialize context data from JSON", e);
        }
    }
}