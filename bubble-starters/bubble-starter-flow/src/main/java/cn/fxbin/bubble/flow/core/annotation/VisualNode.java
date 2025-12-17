package cn.fxbin.bubble.flow.core.annotation;

import cn.fxbin.bubble.flow.core.enums.PluginType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * VisualNode
 * 可视化节点注解
 * 用于标记工作流中可视化节点组件，提供节点的元信息
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface VisualNode {

    /**
     * 节点名称
     * 在可视化界面中显示的节点名称
     */
    String name();

    /**
     * 节点分类
     * 用于在节点面板中对节点进行分组展示
     */
    PluginType pluginType();

    /**
     * 节点描述
     * 对节点功能的详细说明
     */
    String desc();

    /**
     * 节点配置类
     * 用于定义节点的可配置参数结构
     * 默认为 Void.class 表示无需配置
     */
    Class<?> configClazz() default Void.class;

}
