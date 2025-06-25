package cn.fxbin.bubble.plugin.logging.autoconfigure;

import cn.fxbin.bubble.plugin.logging.aspect.LogServiceAspect;
import cn.fxbin.bubble.plugin.logging.aspect.LogWebAspect;
import cn.fxbin.bubble.plugin.logging.properties.LoggingProperties;
import cn.fxbin.bubble.plugin.logging.util.LogFactory;
import cn.fxbin.bubble.plugin.logging.util.TracerUtils;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.digest.manager.SofaTracerDigestReporterAsyncManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * LoggingAutoConfiguration
 *
 * <p>
 * Bubble日志框架的自动配置类，负责初始化和配置日志相关的组件。
 * 基于Spring Boot的自动配置机制，根据配置属性动态创建和配置日志切面。
 * </p>
 *
 * <p>
 * 主要功能：
 * - 启用LoggingProperties配置属性绑定
 * - 根据配置条件创建Web层和服务层日志切面
 * - 扫描并注册日志相关的组件
 * - 提供日志工厂的默认实现
 * </p>
 *
 * <p>
 * 配置说明：
 * - bubble.logging.enabled: 控制整个日志框架的启用状态
 * - bubble.logging.web.enabled: 控制Web层日志切面的启用状态
 * - bubble.logging.service.enabled: 控制服务层日志切面的启用状态
 * </p>
 *
 * <p>
 * 组件扫描：
 * - 扫描cn.fxbin.bubble.plugin.logging包下的所有组件
 * - 自动注册日志工厂、切面等核心组件
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/24 17:27
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(name = "bubble.logging.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "cn.fxbin.bubble.plugin.logging")
public class LoggingAutoConfiguration {

    /**
     * 日志配置属性
     * 通过@EnableConfigurationProperties自动注入
     */
    private final LoggingProperties loggingProperties;

    /**
     * 创建Web层日志切面Bean
     * 
     * <p>
     * 根据配置条件创建LogWebAspect实例。只有在Web应用环境下
     * 且启用了Web层日志记录时才会创建此Bean。
     * </p>
     * 
     * <p>
     * 条件说明：
     * - bubble.logging.web.enabled为true（默认为true）
     * - 当前不存在LogWebAspect类型的Bean
     * </p>
     * 
     * @return LogWebAspect实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "bubble.logging.web.enabled", havingValue = "true", matchIfMissing = true)
    public LogWebAspect logWebAspect() {
        log.info("Creating LogWebAspect with configuration: {}", loggingProperties.getWeb());
        return new LogWebAspect(loggingProperties);
    }

    /**
     * 创建服务层日志切面Bean
     * 
     * <p>
     * 根据配置条件创建LogServiceAspect实例。用于拦截和记录
     * 业务服务层方法的执行日志。
     * </p>
     * 
     * <p>
     * 条件说明：
     * - bubble.logging.service.enabled为true（默认为true）
     * - 当前不存在LogServiceAspect类型的Bean
     * </p>
     * 
     * @return LogServiceAspect实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "bubble.logging.service.enabled", havingValue = "true", matchIfMissing = true)
    public LogServiceAspect logServiceAspect() {
        log.info("Creating LogServiceAspect with configuration: {}", loggingProperties.getService());
        return new LogServiceAspect(loggingProperties);
    }

    /**
     * 创建日志工厂Bean
     * 
     * <p>
     * 提供LogFactory的默认实现，用于日志系统的初始化和配置管理。
     * 只有在当前上下文中不存在LogFactory类型的Bean时才会创建。
     * </p>
     * 
     * @return LogFactory实例
     */
    @Bean
    @ConditionalOnMissingBean
    public LogFactory logFactory() {
        log.info("Creating LogFactory for logging system initialization");
        return new LogFactory();
    }

    /**
     * 创建TracerUtils Bean
     * 
     * <p>
     * 提供TracerUtils的实例，用于获取SOFATracer的分布式追踪信息。
     * 只有在SOFATracer相关类存在且当前上下文中不存在TracerUtils类型的Bean时才会创建。
     * </p>
     * 
     * @return TracerUtils实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({SofaTracerConfiguration.class, SofaTracerDigestReporterAsyncManager.class})
    public TracerUtils tracerUtils() {
        log.info("Creating TracerUtils for SOFATracer integration");
        return new TracerUtils();
    }

}
