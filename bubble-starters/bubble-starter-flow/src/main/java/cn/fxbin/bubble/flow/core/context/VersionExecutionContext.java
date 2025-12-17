package cn.fxbin.bubble.flow.core.context;

import cn.fxbin.bubble.flow.core.model.entity.FlowVersionHistory;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 版本执行上下文
 * 用于在历史版本执行时传递版本相关信息
 *
 * @author fxbin
 * @since 2025/06/25
 */
@Data
@Accessors(chain = true)
public class VersionExecutionContext {
    
    /**
     * 目标执行版本号
     */
    private Integer targetVersion;
    
    /**
     * 版本历史记录快照
     */
    private FlowVersionHistory versionSnapshot;
    
    /**
     * 是否为历史版本执行模式
     */
    private Boolean historicalExecution = false;
    
    /**
     * 版本加载策略
     */
    private VersionLoadStrategy loadStrategy = VersionLoadStrategy.LAZY;
    
    /**
     * 版本验证级别
     */
    private VersionValidationLevel validationLevel = VersionValidationLevel.STRICT;
    
    /**
     * 版本加载策略枚举
     */
    public enum VersionLoadStrategy {
        // 懒加载，按需加载节点定义
        LAZY,
        // 预加载，提前加载完整流程定义
        EAGER
    }
    
    /**
     * 版本验证级别枚举
     */
    public enum VersionValidationLevel {
        // 严格验证，任何不匹配都拒绝执行
        STRICT,
        // 宽松验证，允许部分不匹配
        LENIENT,
        // 不验证，直接执行
        NONE
    }
}