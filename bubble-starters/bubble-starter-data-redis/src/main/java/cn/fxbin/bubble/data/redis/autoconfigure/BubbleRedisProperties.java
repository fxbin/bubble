package cn.fxbin.bubble.data.redis.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.Duration;

import static cn.fxbin.bubble.data.redis.autoconfigure.BubbleRedisProperties.PREFIX;

/**
 * DmRedisProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/11 14:37
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = PREFIX)
public class BubbleRedisProperties {

    public static final String PREFIX = "bubble.data.redis";

    /**
     * stream
     */
    private Stream stream = new Stream();


    @Data
    public static class Stream {
        public static final String PREFIX = BubbleRedisProperties.PREFIX + ".stream";

        /**
         * 是否开启 stream
         */
        boolean enabled = false;

        /**
         * consumer group，默认：服务名 + 环境
         */
        String consumerGroup;

        /**
         * 消费者名称，默认：ip + 端口
         */
        String consumerName;

        /**
         * poll 批量大小
         */
        Integer pollBatchSize;

        /**
         * poll 超时时间
         */
        Duration pollTimeout;
    }
}
