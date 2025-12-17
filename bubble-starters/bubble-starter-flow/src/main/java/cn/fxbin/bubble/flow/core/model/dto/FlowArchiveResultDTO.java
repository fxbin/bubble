package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.flow.core.enums.ArchiveStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlowArchiveResultDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/9/8 10:21
 */
@Data
public class FlowArchiveResultDTO implements Serializable {

    /**
     * 流程ID
     */
    @Schema(description = "流程ID")
    private Long flowId;

    /**
     * 归档策略
     */
    @Schema(description = "归档策略")
    private ArchiveStrategy strategy;

    /**
     * 候选归档版本
     */
    @Schema(description = "候选归档版本")
    private List<Integer> candidateVersions;
    /**
     * 实际归档版本
     */
    @Schema(description = "实际归档版本")
    private List<Integer> archivedVersions;
    /**
     * 跳过的版本
     */
    @Schema(description = "跳过的版本")
    private List<Integer> skippedVersions;
    /**
     * 归档统计信息
     */
    @Schema(description = "归档统计信息")
    private Map<String, Object> statistics;
    /**
     * 警告信息
     */
    @Schema(description = "警告信息")
    private List<String> warnings;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    public FlowArchiveResultDTO(Long flowId, ArchiveStrategy strategy) {
        this.flowId = flowId;
        this.strategy = strategy;
        this.candidateVersions = new ArrayList<>();
        this.archivedVersions = new ArrayList<>();
        this.skippedVersions = new ArrayList<>();
        this.statistics = new HashMap<>();
        this.warnings = new ArrayList<>();
        this.executeTime = LocalDateTime.now();
    }

}
