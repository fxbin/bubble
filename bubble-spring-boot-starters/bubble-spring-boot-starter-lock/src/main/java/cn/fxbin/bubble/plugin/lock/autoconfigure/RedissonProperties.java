package cn.fxbin.bubble.plugin.lock.autoconfigure;

import lombok.Data;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * RedissonProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 15:57
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "spring.redisson")
public class RedissonProperties {
    
    private Config config;

}
