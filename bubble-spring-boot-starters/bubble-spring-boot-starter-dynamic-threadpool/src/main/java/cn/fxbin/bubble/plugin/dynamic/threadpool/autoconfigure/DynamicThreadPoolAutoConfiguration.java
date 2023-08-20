package cn.fxbin.bubble.plugin.dynamic.threadpool.autoconfigure;

import cn.fxbin.bubble.plugin.dynamic.threadpool.autoconfigure.endpoint.ThreadPoolEndpoint;
import cn.fxbin.bubble.core.util.CollectionUtils;
import cn.fxbin.bubble.plugin.dynamic.threadpool.customizer.ResizableCapacityLinkedBlockIngQueue;
import cn.fxbin.bubble.plugin.dynamic.threadpool.enums.QueueType;
import cn.fxbin.bubble.plugin.dynamic.threadpool.enums.RejectedPolicy;
import cn.fxbin.bubble.plugin.dynamic.threadpool.event.ThreadContextRefreshEvent;
import cn.fxbin.bubble.plugin.dynamic.threadpool.support.ThreadPoolExecutorOperations;
import cn.fxbin.bubble.plugin.dynamic.threadpool.support.ThreadPoolRejectedRecordOperations;
import cn.fxbin.bubble.plugin.dynamic.threadpool.wrapper.RejectedExecutionHandlerWrapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static cn.fxbin.bubble.plugin.dynamic.threadpool.autoconfigure.DynamicThreadPoolProperties.BUBBLE_DYNAMIC_THREAD_POOL_PREFIX;

/**
 * ThreadPoolAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/6 16:11
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({ThreadContextRefreshEvent.class, ThreadPoolExecutorOperations.class})
@EnableConfigurationProperties(DynamicThreadPoolProperties.class)
@ConditionalOnProperty(prefix = BUBBLE_DYNAMIC_THREAD_POOL_PREFIX, name = "enabled", matchIfMissing = true)
public class DynamicThreadPoolAutoConfiguration implements InitializingBean {

    @Resource
    private DynamicThreadPoolProperties dynamicThreadPoolProperties;

    /**
     * The name of the SpringBoot Application Name
     */
    public static final String APPLICATION_NAME = "spring.application.name";

    @Resource
    private Environment environment;

    private final ThreadPoolExecutorOperations executorOperations = new ThreadPoolExecutorOperations();

    private final ThreadPoolRejectedRecordOperations rejectedRecordOperations = new ThreadPoolRejectedRecordOperations();

    @Bean
    public ThreadPoolEndpoint threadPoolEndpoint() {
        return new ThreadPoolEndpoint(executorOperations, rejectedRecordOperations);
    }

    @Bean(destroyMethod = "clear")
    public ThreadPoolExecutorOperations executorOperations() {
        return executorOperations;
    }

    @Bean(destroyMethod = "clear")
    public ThreadPoolRejectedRecordOperations rejectedRecordOperations() {
        return rejectedRecordOperations;
    }

    @PostConstruct
    public void initialize() {
        loadThreadPool();
    }

    @EventListener({ThreadContextRefreshEvent.class})
    public void refreshThreadConfig(Object object) {
        ThreadContextRefreshEvent event = (ThreadContextRefreshEvent) object;

        Map<String, Object> propertyMap = event.getPropertyMap();
        refresh(propertyMap);

        dynamicThreadPoolProperties.getPool().forEach(threadPoolProperty -> {
            ThreadPoolExecutor executor = executorOperations.get(threadPoolProperty.getPoolName());
            executor.setCorePoolSize(threadPoolProperty.getCorePoolSize());
            executor.setMaximumPoolSize(threadPoolProperty.getMaximumPoolSize());
            executor.setKeepAliveTime(threadPoolProperty.getKeepAliveTime(), threadPoolProperty.getTimeUnit());
            executor.setRejectedExecutionHandler(RejectedPolicy.match(threadPoolProperty.getRejectedPolicy(), RejectedPolicy.AbortPolicy));
            BlockingQueue<Runnable> queue = executor.getQueue();
            if (queue instanceof ResizableCapacityLinkedBlockIngQueue) {
                ((ResizableCapacityLinkedBlockIngQueue<Runnable>) queue).setCapacity(threadPoolProperty.getCapacity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void loadThreadPool() {
        if (CollectionUtils.isNotEmpty(dynamicThreadPoolProperties.getPool())) {
            dynamicThreadPoolProperties.getPool().forEach(poolProperty -> {
                if (!executorOperations.containsKey(poolProperty.getPoolName())) {
                    ThreadPoolExecutor executor = new ThreadPoolExecutor(
                            poolProperty.getCorePoolSize(),
                            poolProperty.getMaximumPoolSize(),
                            poolProperty.getKeepAliveTime(),
                            poolProperty.getTimeUnit(),
                            QueueType.getQueue(poolProperty.getQueueType(), poolProperty.getCapacity(), poolProperty.isFair()),
                            new ThreadFactoryBuilder().setNameFormat(poolProperty.getPoolName()).build(),
                            new RejectedExecutionHandlerWrapper(
                                    poolProperty.getPoolName(), rejectedRecordOperations,
                                    RejectedPolicy.match(poolProperty.getRejectedPolicy(), RejectedPolicy.AbortPolicy)));
                    executorOperations.put(poolProperty.getPoolName(), executor);
                }
            });
        }
    }

    /**
     * refresh 刷新配置到目标实例
     *
     * @since 2020/7/9 11:05
     * @param propertyMap map 结构配置项
     */
    private void refresh(Map<String, Object> propertyMap) {
        ConfigurationPropertySource configurationPropertySource = new MapConfigurationPropertySource(propertyMap);
        Binder binder = new Binder(configurationPropertySource);
        binder.bind(BUBBLE_DYNAMIC_THREAD_POOL_PREFIX, Bindable.ofInstance(dynamicThreadPoolProperties));
    }

    /***
     * getApplicationName
     *
     * @since 2020/7/7 17:21
     * @return java.lang.String
     */
    private String getApplicationName() {
        String applicationName = environment.getProperty(APPLICATION_NAME);
        return Optional.ofNullable(applicationName).orElse("default");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dynamicThreadPoolProperties.setApplicationName(getApplicationName());
    }



}
