package cn.fxbin.bubble.flow.core.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * FlowTenantAutoConfiguration
 *
 * @author fxbin
 * @since 2025/12/16
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "bubble.flow.tenant", name = "enabled", havingValue = "true")
public class FlowTenantAutoConfiguration {

    @Bean
    public FlowTenantHandler flowTenantHandler(FlowProperties flowProperties) {
        return new FlowTenantHandler(flowProperties);
    }

    @Bean
    @ConditionalOnBean(MybatisPlusInterceptor.class)
    public BeanPostProcessor tenantInterceptorPostProcessor(FlowTenantHandler flowTenantHandler) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof MybatisPlusInterceptor) {
                    MybatisPlusInterceptor interceptor = (MybatisPlusInterceptor) bean;
                    TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor(flowTenantHandler);
                    // Add as first interceptor usually, or just add it
                    interceptor.addInnerInterceptor(tenantInterceptor);
                }
                return bean;
            }
        };
    }
}
