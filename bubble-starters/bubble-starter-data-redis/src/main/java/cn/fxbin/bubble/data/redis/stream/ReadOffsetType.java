package cn.fxbin.bubble.data.redis.stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.connection.stream.ReadOffset;

/**
 * ReadOffsetType
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/11 11:42
 */
@Getter
@AllArgsConstructor
public enum ReadOffsetType {

    /**
     * 从开始的地方读
     */
    start(ReadOffset.from("0-0")),

    /**
     * 从最近的偏移量读取。
     */
    latest(ReadOffset.latest()),

    /**
     * 读取所有新到达的元素，这些元素的id大于最后一个消费组的id。
     */
    lastConsumed(ReadOffset.lastConsumed());

    /**
     * readOffset
     */
    private final ReadOffset readOffset;

}
