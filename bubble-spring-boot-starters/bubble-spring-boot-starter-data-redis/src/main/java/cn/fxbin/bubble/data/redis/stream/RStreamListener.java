package cn.fxbin.bubble.data.redis.stream;

import java.lang.annotation.*;

/**
 * RStreamListener
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/11 11:41
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RStreamListener {

    /**
     * Queue name
     *
     * @return String
     */
    String name();

    /**
     * consumer group，默认为服务名 + 环境
     *
     * @return String
     */
    String group() default "";

    /**
     * 消息方式，集群模式和广播模式，如果想让所有订阅者收到所有消息，广播是一个不错的选择。
     *
     * @return MessageType
     */
    MessageType messageType() default MessageType.cluster;

    /**
     * readOffsetType，默认：LAST_CONSUMED
     *
     * <p>
     * 0-0 : 从开始的地方读。
     * $ ：表示从尾部开始消费，只接受新消息，当前 Stream 消息会全部忽略。
     * > : 读取所有新到达的元素，这些元素的id大于消费组使用的最后一个元素。
     * </p>
     *
     * @return ReadOffsetModel
     */
    ReadOffsetType offsetType() default ReadOffsetType.lastConsumed;

    /**
     * 自动 ack
     *
     * @return boolean
     */
    boolean autoAcknowledge() default false;

    /**
     * 读取原始的 bytes 数据
     *
     * @return boolean
     */
    boolean readRawBytes() default false;

}
