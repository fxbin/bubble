package cn.fxbin.bubble.flow.core.config;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.property.LiteflowConfig;
import com.yomahub.liteflow.property.LiteflowConfigGetter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LiteFlow 配置类
 *
 * <p>用于定义不同类型的 FlowExecutor Beans，以实现手动和自动调用的资源隔离。
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/5/14 11:14
 */
@Configuration
public class LiteFlowConfig {

    /**
     * 创建手动调用的 FlowExecutor Bean。
     *
     * <p>这个执行器专门用于处理用户手动触发的流程。
     * 可以根据需要为其配置独立的线程池等资源。
     *
     * @return 手动调用的 FlowExecutor
     * @author fxbin
     */
    @Bean(name = "manualFlowExecutor")
    @Primary
    public FlowExecutor manualFlowExecutor() {
        // 这里可以根据需要定制 LiteflowConfig，例如设置不同的线程池
         LiteflowConfig config = LiteflowConfigGetter.get();
         config.setMainExecutorClass("com.yomahub.liteflow.thread.LiteFlowDefaultMainExecutorBuilder");
         config.setMainExecutorWorks(10);
         FlowExecutor executor = new FlowExecutor();
         executor.setLiteflowConfig(config);
         return executor;
    }

    /**
     * 创建自动调用的 FlowExecutor Bean。
     *
     * <p>这个执行器专门用于处理系统自动触发的流程（例如定时任务）。
     * 可以根据需要为其配置独立的线程池等资源，以避免与手动调用互相影响。
     *
     * @return 自动调用的 FlowExecutor
     * @author fxbin
     */
    @Bean(name = "autoFlowExecutor")
    public FlowExecutor autoFlowExecutor() {
        // 这里可以根据需要定制 LiteflowConfig，例如设置不同的线程池
        // 创建新的Config对象以避免影响默认配置
         LiteflowConfig config = new LiteflowConfig();
         // ... 从 LiteflowConfigGetter.get() 复制通用配置，然后修改特定部分 ...
         config.setMainExecutorClass("com.yomahub.liteflow.thread.LiteFlowDefaultMainExecutorBuilder");
         config.setMainExecutorWorks(5);
         FlowExecutor executor = new FlowExecutor();
         executor.setLiteflowConfig(config);
         return executor;
    }
}