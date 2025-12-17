package cn.fxbin.bubble.flow.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * FlowAutoConfiguration
 *
 * @author fxbin
 * @since 2025/12/16
 */
@AutoConfiguration
@EnableConfigurationProperties(FlowProperties.class)
@ConditionalOnProperty(prefix = "bubble.flow", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "cn.fxbin.bubble.flow.core")
@MapperScan("cn.fxbin.bubble.flow.core.mapper")
@Import(LiteFlowConfig.class)
public class FlowAutoConfiguration {
    
}
