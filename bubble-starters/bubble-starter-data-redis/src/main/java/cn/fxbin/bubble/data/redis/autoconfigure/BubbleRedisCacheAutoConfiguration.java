package cn.fxbin.bubble.data.redis.autoconfigure;

import cn.fxbin.bubble.core.constant.CharPool;
import cn.fxbin.bubble.core.constant.StringPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RedisCacheAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/14 20:27
 */
@EnableCaching
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({CacheManager.class, RedisConnectionFactory.class})
@AutoConfigureAfter({RedisTemplateAutoConfiguration.class, CacheAutoConfiguration.class})
@EnableConfigurationProperties(CacheProperties.class)
public class BubbleRedisCacheAutoConfiguration {

    @Primary
    @Bean("cacheResolver")
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                          RedisSerializer<Object> redisSerializer,
                                          CacheProperties cacheProperties,
                                          ObjectProvider<CacheManagerCustomizer<RedisCacheManager>> customizers,
                                          @Nullable ObjectProvider<RedisCacheConfiguration> redisCacheConfigurationProvider) {
        Objects.requireNonNull(connectionFactory, "Bean RedisConnectionFactory is null.");
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisCacheConfiguration cacheConfiguration = this.determineConfiguration(
                redisSerializer, cacheProperties, redisCacheConfigurationProvider.getIfAvailable());
        List<String> cacheNames = cacheProperties.getCacheNames();
        Map<String, RedisCacheConfiguration> initialCaches = new LinkedHashMap<>();
        if (!cacheNames.isEmpty()) {
            Map<String, RedisCacheConfiguration> cacheConfigMap = new LinkedHashMap<>(cacheNames.size());
            cacheNames.forEach(it -> cacheConfigMap.put(it, cacheConfiguration));
            initialCaches.putAll(cacheConfigMap);
        }
        boolean allowInFlightCacheCreation = true;
        boolean enableTransactions = false;
        RedisCacheManager cacheManager = new RedisCacheManager(
                redisCacheWriter, cacheConfiguration, allowInFlightCacheCreation, initialCaches
        );
        cacheManager.setTransactionAware(enableTransactions);
        
        // 应用所有的 CacheManagerCustomizer
        customizers.orderedStream().forEach(customizer -> customizer.customize(cacheManager));
        
        return cacheManager;
    }

    /**
     * 选择配置
     *
     * @param redisSerializer Redis序列化器
     * @param cacheProperties 缓存属性
     * @param redisCacheConfiguration 自定义Redis缓存配置
     * @return {@link RedisCacheConfiguration}
     */
    private RedisCacheConfiguration determineConfiguration(RedisSerializer<Object> redisSerializer,
                                                           CacheProperties cacheProperties,
                                                           @Nullable RedisCacheConfiguration redisCacheConfiguration) {
        if (redisCacheConfiguration != null) {
            return redisCacheConfiguration;
        }
        
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        
        // 设置默认缓存名分割符号为 ":"，如果已经带 ":" 则不设置。
        config = config.computePrefixWith(name -> name.endsWith(StringPool.COLON) ? name : name + CharPool.COLON);
        
        // 设置序列化方式
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
        
        // 配置TTL
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        
        // 配置key前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        
        // 配置是否缓存null值
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        
        // 配置是否使用key前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        
        return config;
    }

}