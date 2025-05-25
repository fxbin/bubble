package cn.fxbin.bubble.data.redis.autoconfigure;

import cn.fxbin.bubble.core.util.ArrayUtils;
import cn.fxbin.bubble.data.redis.RedisOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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
@AutoConfigureAfter(org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
public class RedisTemplateAutoConfiguration {

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
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> bfRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                             RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

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

        return template;
    }


    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
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

        return template;
    }

    /**
     * redisOperations
     *
     * <p>
     *     Redis 操作对象注入
     *     1. redis 基本操作
     *     2. redis 日志操作记录
     * </p>
     *
     * @since 2021/12/2 11:43 上午
     * @param bfRedisTemplate {@link RedisTemplateAutoConfiguration#bfRedisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory, org.springframework.data.redis.serializer.RedisSerializer)}
     * @param stringRedisTemplate {@link RedisTemplateAutoConfiguration#stringRedisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory, org.springframework.data.redis.serializer.RedisSerializer)}
     * @return {@link RedisOperations}
     */
    @Bean
    public RedisOperations redisOperations(@Qualifier("bfRedisTemplate") RedisTemplate bfRedisTemplate,
                                           @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate) {

        RedisOperations redisOperations = new RedisOperations(bfRedisTemplate, stringRedisTemplate);

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(redisOperations);
        proxyFactory.setProxyTargetClass(true);
        // 织入代理拦截器，记录操作日志
        proxyFactory.addAdvice(
                new MethodBeforeAdviceInterceptor(
                        (method, args, target) ->
                                log.info("Redis operate INFO : method = {},params = {}",
                                        method.getName(), ArrayUtils.toString(args))));

        return (RedisOperations) proxyFactory.getProxy();
    }

}
