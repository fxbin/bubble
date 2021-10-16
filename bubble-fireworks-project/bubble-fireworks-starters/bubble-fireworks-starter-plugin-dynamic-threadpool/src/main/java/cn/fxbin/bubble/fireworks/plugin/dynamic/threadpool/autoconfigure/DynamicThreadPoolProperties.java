package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.autoconfigure;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.enums.QueueType;
import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.enums.RejectedPolicy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DynamicThreadPoolProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/6 15:34
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = DynamicThreadPoolProperties.BUBBLE_FIREWORKS_DYNAMIC_THREAD_POOL_PREFIX)
public class DynamicThreadPoolProperties {


    public static final String BUBBLE_FIREWORKS_DYNAMIC_THREAD_POOL_PREFIX = "bubble.fireworks.dynamic.thread";

    private String applicationName;

    private List<DynamicThreadPoolProperties.ThreadPoolProperty> pool = new ArrayList<>();

    @Data
    public static class ThreadPoolProperty {

        /**
         * 联系人，建议邮箱|姓名
         */
        private String contact;

        /**
         * 线程池名称
         */
        private String poolName = "bubble-thread-pool";

        /**
         * 核心线程数
         */
        private Integer corePoolSize = 1;

        /**
         * 线程的最大生命周期
         */
        private Long keepAliveTime = 60L;

        /**
         * keepAliveTime的时间单位，可以是纳秒，毫秒，秒，分钟等
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;

        /**
         * 线程池中能容纳的最大线程数量
         */
        private Integer maximumPoolSize = Math.max(1, Runtime.getRuntime().availableProcessors());

        /**
         * 队列
         */
        private Integer capacity = 1000;

        /**
         * 是否公平锁
         */
        private boolean fair;

        /**
         * 线程池使用的缓冲队列, 默认 java.util.concurrent.LinkedBlockingQueue
         */
        private QueueType queueType = QueueType.LinkedBlockingQueue;

        /**
         * 线程池拒绝策略, 默认 java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
         */
        private String rejectedPolicy = RejectedPolicy.DiscardPolicy.getName();

    }

}


