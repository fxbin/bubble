package cn.fxbin.bubble.fireworks.autoconfigure.data.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

/**
 * RedisAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 13:41
 */
@Slf4j
@Configuration(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackages = "cn.fxbin.bubble.data.redis"
)
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
public class RedisAutoConfiguration {

    /**
     * value 值 序列化
     *
     * @return {@code RedisSerializer}
     */
    @Bean
    @ConditionalOnMissingBean(RedisSerializer.class)
    public RedisSerializer<Object> redisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Serializable> redisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                             RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();

        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory);

        // 设置key/hash key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值序列化
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);

        // 初始化RedisTemplate
        template.afterPropertiesSet();

        log.info("RedisTemplate init... successfully!!!");
        return template;
    }


    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                   RedisSerializer<Object> redisSerializer) {
        StringRedisTemplate template = new StringRedisTemplate();

        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory);

        // 设置key/hash key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值序列化
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);

        // 初始化RedisTemplate
        template.afterPropertiesSet();

        log.info("StringRedisTemplate init... successfully!!!");
        return template;
    }
}
