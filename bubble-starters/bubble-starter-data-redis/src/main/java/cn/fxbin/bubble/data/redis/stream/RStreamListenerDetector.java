package cn.fxbin.bubble.data.redis.stream;

import cn.fxbin.bubble.core.util.ClassUtils;
import cn.fxbin.bubble.core.util.ReflectUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.data.redis.RStreamOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * RStreamListenerDetector
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/11 11:46
 */
@Slf4j
@RequiredArgsConstructor
public class RStreamListenerDetector implements BeanPostProcessor, InitializingBean {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer;

    private final RedisTemplate redisTemplate;

    private final String consumerGroup;

    private final String consumerName;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> userClass = ClassUtils.getUserClass(bean);
        ReflectionUtils.doWithMethods(userClass, method -> {
            RStreamListener listener = AnnotationUtils.findAnnotation(method, RStreamListener.class);
            if (listener != null) {
                String streamKey = listener.name();
                Assert.hasText(streamKey, "@RStreamListener name must not be empty.");
                log.info("Found @RStreamListener on bean:{} method:{}", beanName, method);
                // 校验 method，method 入参数大于等于1
                int paramCount = method.getParameterCount();
                if (paramCount > 1) {
                    throw new IllegalArgumentException("@RStreamListener on method " + method + " parameter count must less or equal to 1.");
                }
                // streamOffset
                ReadOffset readOffset = listener.offsetType().getReadOffset();
                StreamOffset<String> streamOffset = StreamOffset.create(streamKey, readOffset);
                // 消费模式
                MessageType messageType = listener.messageType();
                if (MessageType.broadcast == messageType) {
                    broadCast(streamOffset, bean, method, listener.readRawBytes());
                } else {
                    String groupId = StringUtils.isNotBlank(listener.group()) ? listener.group() : consumerGroup;
                    Consumer consumer = Consumer.from(groupId, consumerName);
                    // 如果需要，创建 group
                    createGroupIfNeed(redisTemplate, streamKey, readOffset, groupId);
                    cluster(consumer, streamOffset, listener, bean, method);
                }
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
        return bean;
    }

    private void broadCast(StreamOffset<String> streamOffset, Object bean, Method method, boolean isReadRawBytes) {
        streamMessageListenerContainer.receive(streamOffset, (message) -> {
            // MapBackedRecord
            invokeMethod(bean, method, message, isReadRawBytes);
        });
    }

    private void cluster(Consumer consumer, StreamOffset<String> streamOffset, RStreamListener listener, Object bean, Method method) {
        boolean autoAcknowledge = listener.autoAcknowledge();
        StreamMessageListenerContainer.ConsumerStreamReadRequest<String> readRequest = StreamMessageListenerContainer.StreamReadRequest.builder(streamOffset).consumer(consumer).autoAcknowledge(autoAcknowledge).build();
        StreamOperations<String, Object, Object> opsForStream = redisTemplate.opsForStream();
        streamMessageListenerContainer.register(readRequest, (message) -> {
            // MapBackedRecord
            invokeMethod(bean, method, message, listener.readRawBytes());
            // ack
            if (!autoAcknowledge) {
                opsForStream.acknowledge(consumer.getGroup(), message);
            }
        });
    }

    private static void createGroupIfNeed(RedisTemplate<String, Object> redisTemplate, String streamKey, ReadOffset readOffset, String group) {
        StreamOperations<String, Object, Object> opsForStream = redisTemplate.opsForStream();
        try {
            StreamInfo.XInfoGroups groups = opsForStream.groups(streamKey);
            if (groups.stream().noneMatch((x) -> group.equals(x.groupName()))) {
                opsForStream.createGroup(streamKey, readOffset, group);
            }
        } catch (RedisSystemException e) {
            // RedisCommandExecutionException: ERR no such key
            opsForStream.createGroup(streamKey, group);
        }
    }

    private void invokeMethod(Object bean, Method method, MapRecord<String, String, byte[]> mapRecord, boolean isReadRawBytes) {
        // 支持没有参数的方法
        if (method.getParameterCount() == 0) {
            ReflectUtils.invokeMethod(method, bean);
            return;
        }
        if (isReadRawBytes) {
            ReflectUtils.invokeMethod(method, bean, mapRecord);
        } else {
            ReflectUtils.invokeMethod(method, bean, getRecordValue(mapRecord));
        }
    }

    private Object getRecordValue(MapRecord<String, String, byte[]> mapRecord) {
        Map<String, byte[]> messageValue = mapRecord.getValue();
        if (messageValue.containsKey(RStreamOperations.OBJECT_PAYLOAD_KEY)) {
            byte[] payloads = messageValue.get(RStreamOperations.OBJECT_PAYLOAD_KEY);
            Object deserialize = redisTemplate.getValueSerializer().deserialize(payloads);
            return ObjectRecord.create(mapRecord.getStream(), deserialize).withId(mapRecord.getId());
        } else {
            return mapRecord.mapEntries(entry -> {
                String key = entry.getKey();
                Object value = redisTemplate.getValueSerializer().deserialize(entry.getValue());
                return Collections.singletonMap(key, value).entrySet().iterator().next();
            });
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        streamMessageListenerContainer.start();
    }
}
