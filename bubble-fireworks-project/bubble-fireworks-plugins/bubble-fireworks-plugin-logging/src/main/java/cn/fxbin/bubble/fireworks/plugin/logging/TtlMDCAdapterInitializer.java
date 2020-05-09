package cn.fxbin.bubble.fireworks.plugin.logging;

import org.slf4j.TtlMDCAdapter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TtlMDCAdapterInitializer
 * 初始化TtlMDCAdapter实例，并替换MDC 中的adapter 对象
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/22 16:13
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class TtlMDCAdapterInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 加载TtlMDCAdapter 实例
        TtlMDCAdapter.getTtlMDCAdapter();
    }
}
