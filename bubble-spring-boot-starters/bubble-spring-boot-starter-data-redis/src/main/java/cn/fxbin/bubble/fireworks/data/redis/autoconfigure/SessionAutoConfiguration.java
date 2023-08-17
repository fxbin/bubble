package cn.fxbin.bubble.fireworks.data.redis.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;

/**
 * SessionConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/27 18:31
 */
@Configuration(
        proxyBeanMethods = false
)
@EnableRedisHttpSession
@ConditionalOnClass({RedisIndexedSessionRepository.class, RedisHttpSessionConfiguration.class})
public class SessionAutoConfiguration {

    /**
     * 创建 {@link RedisIndexedSessionRepository} 使用的 RedisSerializer Bean 。
     *
     * {@link RedisHttpSessionConfiguration#setDefaultRedisSerializer(RedisSerializer)} 方法，
     * 它会引入名字为 "springSessionDefaultRedisSerializer" 的 Bean 。
     *
     * @return RedisSerializer Bean
     */
    @Bean(name = "springSessionDefaultRedisSerializer")
    public RedisSerializer springSessionDefaultRedisSerializer() {
        return RedisSerializer.json();
    }

    /**
     * Redis云服务Unable to configure Redis to keyspace notifications异常
     * @see <a href="#">https://docs.spring.io/spring-session/docs/current/reference/html5/#api-redisoperationssessionrepository-sessiondestroyedevent</a>
     * @return {@link ConfigureRedisAction }
     */
    @Bean
    @ConditionalOnClass(ConfigureRedisAction.class)
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

}
