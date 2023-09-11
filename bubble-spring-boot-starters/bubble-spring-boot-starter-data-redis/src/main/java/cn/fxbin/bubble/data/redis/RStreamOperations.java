package cn.fxbin.bubble.data.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * RStreamOperations
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/11 14:02
 */
@RequiredArgsConstructor
public class RStreamOperations {

    private final RedisTemplate redisTemplate;

    private final StreamOperations<String, String, Object> streamOperations;
    private static final RedisCustomConversions CUSTOM_CONVERSIONS = new RedisCustomConversions();

    public static final String OBJECT_PAYLOAD_KEY = "@payload";

    public RStreamOperations(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.streamOperations = redisTemplate.opsForStream();
    }

    /**
     * 发布消息
     *
     * @param name  名称
     * @param value 值
     * @return {@link RecordId}
     */
    public RecordId send(String name, Object value) {
        return this.send(ObjectRecord.create(name, value));
    }

    /**
     * 发布消息
     *
     * @param name  名称
     * @param key   消息key
     * @param value 值
     * @return {@link RecordId}
     */
    public RecordId send(String name, String key, Object value) {
        return this.send(name, Collections.singletonMap(key, value));
    }

    /**
     * 发送消息
     *
     * @param name   名称
     * @param key    消息key
     * @param data   数据
     * @param mapper 映射器
     * @return {@link RecordId}
     */
    public <T> RecordId send(String name, String key, T data, Function<T, byte[]> mapper) {
        return this.send(name, key, mapper.apply(data));
    }

    /**
     * 发送消息
     *
     * @param name     名称
     * @param messages 消息
     * @return {@link RecordId}
     */
    public RecordId send(String name, Map<String, Object> messages) {
        return this.send(MapRecord.create(name, messages));
    }

    /**
     * 发布消息
     *
     * @param record 记录
     * @return {@link RecordId}
     */
    public RecordId send(Record<String, ?> record) {
        // 1. MapRecord
        if (record instanceof MapRecord) {
            return streamOperations.add(record);
        }
        String stream = Objects.requireNonNull(record.getStream(), "RStreamTemplate send stream name is null.");
        Object recordValue = Objects.requireNonNull(record.getValue(), () -> "RStreamTemplate send stream: " + stream + " value is null.");
        Class<?> valueClass = recordValue.getClass();
        // 2. 普通类型的 ObjectRecord
        if (CUSTOM_CONVERSIONS.isSimpleType(valueClass)) {
            return streamOperations.add(record);
        }
        // 3. 自定义类型处理
        Map<String, Object> payload = new HashMap<>();

        // 自定义 pojo 类型 key
        payload.put(OBJECT_PAYLOAD_KEY, recordValue);
        MapRecord<String, String, Object> mapRecord = MapRecord.create(stream, payload);
        return streamOperations.add(mapRecord);
    }

    /**
     * 发布消息
     *
     * @param name 名称
     * @param key  消息key
     * @param data 数据
     * @return {@link RecordId}
     */
    public RecordId send(String name, String key, byte[] data) {
        return this.send(name, key, data, RedisStreamCommands.XAddOptions.none());
    }

    /**
     * 邮寄
     *
     * @param name   名称
     * @param key    消息key
     * @param data   数据
     * @param maxLen 限制 stream 最大长度
     * @return {@link RecordId}
     */
    public RecordId send(String name, String key, byte[] data, long maxLen) {
        return this.send(name, key, data, RedisStreamCommands.XAddOptions.maxlen(maxLen));
    }

    /**
     * 邮寄
     *
     * @param name   名称
     * @param key    消息key
     * @param data   数据
     * @param mapper mapper
     * @param maxLen 限制 stream 最大长度
     * @return {@link RecordId}
     */
    public <T> RecordId send(String name, String key, T data, Function<T, byte[]> mapper, long maxLen) {
        return send(name, key, mapper.apply(data), maxLen);
    }

    /**
     * 邮寄
     *
     * @param name    名称
     * @param key     消息key
     * @param data    数据
     * @param mapper  mapper
     * @param options {@link RedisStreamCommands.XAddOptions}
     * @return {@link RecordId}
     */
    public <T> RecordId send(String name, String key, T data, Function<T, byte[]> mapper, RedisStreamCommands.XAddOptions options) {
        return this.send(name, key, mapper.apply(data), options);
    }

    /**
     * 邮寄
     *
     * @param name    名称
     * @param key     钥匙
     * @param data    数据
     * @param options {@link RedisStreamCommands.XAddOptions}
     * @return {@link RecordId}
     */
    public RecordId send(String name, String key, byte[] data, RedisStreamCommands.XAddOptions options) {
        RedisSerializer<String> stringSerializer = StringRedisSerializer.UTF_8;
        byte[] nameBytes = Objects.requireNonNull(stringSerializer.serialize(name), "redis stream name is null.");
        byte[] keyBytes = Objects.requireNonNull(stringSerializer.serialize(key), "redis stream key is null.");
        Map<byte[], byte[]> mapDate = Collections.singletonMap(keyBytes, data);
        return (RecordId) redisTemplate.execute((RedisCallback<RecordId>) redis -> {
            RedisStreamCommands streamCommands = redis.streamCommands();
            return streamCommands.xAdd(MapRecord.create(nameBytes, mapDate), options);
        });
    }

    /**
     * 删除消息
     *
     * @param name      名称
     * @param recordIds 记录ID
     * @return {@link Long}
     */
    public Long delete(String name, String... recordIds) {
        return streamOperations.delete(name, recordIds);
    }

    /**
     * 删除消息
     *
     * @param name      名称
     * @param recordIds 记录ID
     * @return {@link Long}
     */
    public Long delete(String name, RecordId... recordIds) {
        return streamOperations.delete(name, recordIds);
    }

    /**
     * 删除消息
     *
     * @param record 记录
     * @return {@link Long}
     */
    public Long delete(Record<String, ?> record) {
        return streamOperations.delete(record.getStream(), record.getId());
    }

    /**
     * 对流进行修剪，限制长度
     *
     * @param name  名称
     * @param count 计数
     * @return {@link Long}
     */
    public Long trim(String name, long count) {
        return trim(name, count, false);
    }

    /**
     * 对流进行修剪，限制长度
     *
     * @param name                名称
     * @param count               计数
     * @param approximateTrimming 近似修整
     * @return {@link Long}
     */
    public Long trim(String name, long count, boolean approximateTrimming) {
        return streamOperations.trim(name, count, approximateTrimming);
    }

    /**
     * 手动 ack
     *
     * @param name      名称
     * @param group     组
     * @param recordIds 记录ID
     * @return {@link Long}
     */
    public Long acknowledge(String name, String group, String... recordIds) {
        return streamOperations.acknowledge(name, group, recordIds);
    }

    /**
     * 手动 ack
     *
     * @param name      名称
     * @param group     组
     * @param recordIds 记录ID
     * @return {@link Long}
     */
    public Long acknowledge(String name, String group, RecordId... recordIds) {
        return streamOperations.acknowledge(name, group, recordIds);
    }

    /**
     * 手动 ack
     *
     * @param group  组
     * @param record 记录
     * @return {@link Long}
     */
    public Long acknowledge(String group, Record<String, ?> record) {
        return streamOperations.acknowledge(group, record);
    }

}
