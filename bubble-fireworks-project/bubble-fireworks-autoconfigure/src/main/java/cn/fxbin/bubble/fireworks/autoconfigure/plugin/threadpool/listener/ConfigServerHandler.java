package cn.fxbin.bubble.fireworks.autoconfigure.plugin.threadpool.listener;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.event.ThreadContextRefreshEvent;
import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.handler.ConfigListenerHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.fxbin.bubble.fireworks.autoconfigure.plugin.threadpool.DynamicThreadPoolProperties.BUBBLE_FIREWORKS_DYNAMIC_THREAD_POOL_PREFIX;

/**
 * ConfigServerHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/8 14:34
 */
public class ConfigServerHandler implements ConfigListenerHandler {

    @Resource
    private ApplicationContext context;

    @Resource
    private ConfigServicePropertySourceLocator sourceLocator;

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public ConfigServerHandler() {
        refreshConfig();
    }

    /**
     * refreshConfig 刷新配置
     *
     * @since 2020/7/8 14:33
     */
    @Override
    public void refreshConfig() {
        executorService.scheduleAtFixedRate(() -> {

            Map<String, Object> propertyResult = new HashMap<>();
            PropertySource<?> propertySource = sourceLocator.locate(context.getEnvironment());
            for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                if (key.contains(BUBBLE_FIREWORKS_DYNAMIC_THREAD_POOL_PREFIX)) {
                    propertyResult.put(key, propertySource.getProperty(key));
                }
            }

            this.context.publishEvent(new ThreadContextRefreshEvent(propertyResult));

        }, 2, 10, TimeUnit.SECONDS);

    }


}
