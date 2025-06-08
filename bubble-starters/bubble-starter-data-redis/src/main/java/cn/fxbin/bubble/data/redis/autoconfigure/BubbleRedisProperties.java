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

    /**
     * redisson
     */
    private Redisson redisson = new Redisson();


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

    @Data
    public static class Redisson {
        public static final String PREFIX = BubbleRedisProperties.PREFIX + ".redisson";

        /**
         * 是否启用 Redisson
         */
        boolean enabled = false;

        /**
         * Redisson 配置文件路径
         */
        String configLocation;

        /**
         * 连接池大小
         */
        Integer connectionPoolSize = 64;

        /**
         * 最小空闲连接数
         */
        Integer connectionMinimumIdleSize = 10;

        /**
         * 连接空闲超时时间（毫秒）
         */
        Integer idleConnectionTimeout = 10000;

        /**
         * 连接超时时间（毫秒）
         */
        Integer connectTimeout = 10000;

        /**
         * 命令等待超时时间（毫秒）
         */
        Integer timeout = 3000;

        /**
         * 命令失败重试次数
         */
        Integer retryAttempts = 3;

        /**
         * 命令重试发送时间间隔（毫秒）
         */
        Integer retryInterval = 1500;

        /**
         * 单个连接最大订阅数量
         */
        Integer subscriptionsPerConnection = 5;

        /**
         * 客户端名称
         */
        String clientName;

        /**
         * 线程池数量，默认值 = 当前处理核数量 * 2
         */
        Integer threads;

        /**
         * Netty线程池数量，默认值 = 当前处理核数量 * 2
         */
        Integer nettyThreads;
    }
}
