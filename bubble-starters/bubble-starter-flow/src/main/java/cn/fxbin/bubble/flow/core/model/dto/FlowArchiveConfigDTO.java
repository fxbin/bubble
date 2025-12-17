package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.flow.core.enums.ArchiveStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * FlowArchiveConfig
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/9/8 10:34
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowArchiveConfigDTO implements Serializable {

    @Schema(description = "归档策略")
    private ArchiveStrategy strategy;

    /**
     * 保留天数（BY_TIME策略）
     */
    @Schema(description = "保留天数")
    private Integer retentionDays;

    /**
     * 保留版本数（BY_COUNT策略）
     */
    @Schema(description = "保留版本数")
    private Integer retentionCount;

    /**
     * 最小使用次数（BY_USAGE策略）
     */
    @Schema(description = "最小使用次数")
    private Integer minUsageCount;

    /**
     * 手动指定版本（MANUAL策略）
     */
    @Schema(description = "手动指定版本")
    private List<Integer> manualVersions;

    /**
     * 是否为试运行
     */
    @Schema(description = "是否为试运行")
    private boolean dryRun = false;

    /**
     * 归档前是否备份
     */
    @Schema(description = "归档前是否备份")
    private boolean backupBeforeArchive = true;

    public static FlowArchiveConfigDTO byTime(int retentionDays) {
        FlowArchiveConfigDTO config = new FlowArchiveConfigDTO();
        config.strategy = ArchiveStrategy.BY_TIME;
        config.retentionDays = retentionDays;
        return config;
    }

    public static FlowArchiveConfigDTO byCount(int retentionCount) {
        FlowArchiveConfigDTO config = new FlowArchiveConfigDTO();
        config.strategy = ArchiveStrategy.BY_COUNT;
        config.retentionCount = retentionCount;
        return config;
    }

    public static FlowArchiveConfigDTO byUsage(int minUsageCount) {
        FlowArchiveConfigDTO config = new FlowArchiveConfigDTO();
        config.strategy = ArchiveStrategy.BY_USAGE;
        config.minUsageCount = minUsageCount;
        return config;
    }

    public static FlowArchiveConfigDTO manual(List<Integer> versions) {
        FlowArchiveConfigDTO config = new FlowArchiveConfigDTO();
        config.strategy = ArchiveStrategy.MANUAL;
        config.manualVersions = versions;
        return config;
    }
    
}
