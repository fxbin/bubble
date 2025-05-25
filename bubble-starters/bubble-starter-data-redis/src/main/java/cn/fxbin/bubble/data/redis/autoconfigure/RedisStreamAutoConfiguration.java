package cn.fxbin.bubble.data.redis.autoconfigure;

import cn.fxbin.bubble.core.constant.SpringEnvConst;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.data.redis.RStreamOperations;
import cn.fxbin.bubble.data.redis.stream.RStreamListenerDetector;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import java.time.Duration;

/**
 * RedisStreamAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/11 14:35
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnProperty(prefix = BubbleRedisProperties.Stream.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(BubbleRedisProperties.class)
@AutoConfigureAfter(RedisTemplateAutoConfiguration.class)
public class RedisStreamAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, byte[]>> streamMessageListenerContainerOptions(BubbleRedisProperties properties,
                                                                                                                                                                 ObjectProvider<ErrorHandler> errorHandlerObjectProvider) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptionsBuilder<String, MapRecord<String, String, byte[]>> builder = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .keySerializer(RedisSerializer.string())
                .hashKeySerializer(RedisSerializer.string())
                .hashValueSerializer(RedisSerializer.byteArray());
        BubbleRedisProperties.Stream streamProperties = properties.getStream();
        // 批量大小
        Integer pollBatchSize = streamProperties.getPollBatchSize();
        if (pollBatchSize != null && pollBatchSize > 0) {
            builder.batchSize(pollBatchSize);
        }
        // poll 超时时间
        Duration pollTimeout = streamProperties.getPollTimeout();
        if (pollTimeout != null && !pollTimeout.isNegative()) {
            builder.pollTimeout(pollTimeout);
        }
        // errorHandler
        errorHandlerObjectProvider.ifAvailable((builder::errorHandler));
        // TODO L.cm executor
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                                                                    StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, byte[]>> streamMessageListenerContainerOptions) {
        // 根据配置对象创建监听容器
        return StreamMessageListenerContainer.create(redisConnectionFactory, streamMessageListenerContainerOptions);
    }

    @Bean
    @ConditionalOnMissingBean
    public RStreamListenerDetector streamListenerDetector(StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer,
                                                          RedisTemplate<String, Object> redisTemplate,
                                                          ObjectProvider<ServerProperties> serverPropertiesObjectProvider,
                                                          BubbleRedisProperties properties,
                                                          Environment environment) {
        BubbleRedisProperties.Stream streamProperties = properties.getStream();
        // 消费组名称
        String consumerGroup = streamProperties.getConsumerGroup();
        if (StringUtils.isBlank(consumerGroup)) {
            String appName = environment.getRequiredProperty(SpringEnvConst.APPLICATION_NAME);
            String profile = environment.getProperty(SpringEnvConst.SPRING_PROFILES_ACTIVE);
            consumerGroup = StringUtils.isBlank(profile) ? appName : appName + CharPool.COLON + profile;
        }
        // 消费者名称
        String consumerName = streamProperties.getConsumerName();
        if (StringUtils.isBlank(consumerName)) {
            final StringBuilder consumerNameBuilder = new StringBuilder(NetUtil.getLocalhostStr());
            serverPropertiesObjectProvider.ifAvailable(serverProperties -> {
                consumerNameBuilder.append(CharPool.COLON).append(serverProperties.getPort());
            });
            consumerName = consumerNameBuilder.toString();
        }
        return new RStreamListenerDetector(streamMessageListenerContainer, redisTemplate, consumerGroup, consumerName);
    }

    @Bean
    public RStreamOperations streamOperations(@Qualifier("bfRedisTemplate") RedisTemplate dmRedisTemplate) {
        return new RStreamOperations(dmRedisTemplate);
    }

}
