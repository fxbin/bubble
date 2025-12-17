package cn.fxbin.bubble.flow.core.state.serializer;

import java.util.Map;

/**
 * 流上下文状态序列化与反序列化的接口。
 *
 * @author fxbin
 * @since 2025/4/22
 */
public interface FlowStateSerializer {

    /**
     * 将给定的上下文数据Map序列化为字符串表示形式。
     *
     * @param contextData 包含上下文数据的Map(如变量、开始时间、版本等)
     * @return 上下文数据的字符串表示
     * @throws SerializationException 如果序列化过程中发生错误
     */
    String serialize(Map<String, Object> contextData) throws SerializationException;

    /**
     * 将给定的字符串表示形式反序列化为上下文数据Map。
     *
     * @param serializedContext 上下文数据的字符串表示
     * @return 包含反序列化后上下文数据的Map
     * @throws SerializationException 如果反序列化过程中发生错误或数据损坏
     */
    Map<String, Object> deserialize(String serializedContext) throws SerializationException;

}