package cn.fxbin.bubble.flow.core.state.serializer;

/**
 * 流状态序列化/反序列化过程中发生错误时的自定义异常。
 *
 * @author fxbin
 * @since 2025/4/22
 */
public class SerializationException extends RuntimeException {

    /**
     * 使用指定的详细消息构造一个新的序列化异常。
     *
     * @param message 详细消息
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的序列化异常。
     *
     * @param message 详细消息
     * @param cause 原因
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}