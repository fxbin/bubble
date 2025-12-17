package cn.fxbin.bubble.flow.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.fxbin.bubble.flow.core.enums.ArchiveStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 归档操作日志实体
 * 
 * @author fxbin
 * @since 2025-09-05 17:58
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_archive_operation_log")
public class FlowArchiveOperationLog extends BizEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 操作ID（用于幂等性）
     */
    @Schema(description = "操作ID")
    private String operationId;

    /**
     * 流程ID
     */
    @Schema(description = "流程ID")
    private Long flowId;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 操作类型(ARCHIVE/RESTORE)
     */
    @Schema(description = "操作类型，ARCHIVE/RESTORE")
    private String operationType;

    /**
     * 归档策略
     */
    @Schema(description = "归档策略")
    private ArchiveStrategy archiveStrategy;

    /**
     * 操作状态(SUCCESS/FAILED)
     */
    @Schema(description = "操作状态，SUCCESS/FAILED")
    private String operationStatus;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 耗时(毫秒)
     */
    @Schema(description = "耗时(毫秒)")
    private Integer durationMs;

    /**
     * 影响行数
     */
    @Schema(description = "影响行数")
    private Integer affectedRows;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 操作人
     */
    @Schema(description = "操作人ID")
    private Long operatorId;

    /**
     * 备份快照
     */
    @TableField("backup_snapshot")
    @Schema(description = "备份快照")
    private String backupSnapshot;

    /**
     * 记录操作开始
     */
    public void startOperation(String operationId, Long flowId, Integer version, String operationType, ArchiveStrategy archiveStrategy) {
        this.operationId = operationId;
        this.flowId = flowId;
        this.version = version;
        this.operationType = operationType;
        this.archiveStrategy = archiveStrategy;
        this.startTime = LocalDateTime.now();
        this.operationStatus = "PROCESSING";
    }

    /**
     * 记录操作成功
     */
    public void completeOperation(int affectedRows) {
        this.endTime = LocalDateTime.now();
        this.durationMs = (int) Duration.between(startTime, endTime).toMillis();
        this.affectedRows = affectedRows;
        this.operationStatus = "SUCCESS";
    }

    /**
     * 记录操作失败
     */
    public void failOperation(String errorMessage) {
        this.endTime = LocalDateTime.now();
        this.durationMs = (int) Duration.between(startTime, endTime).toMillis();
        this.errorMessage = errorMessage;
        this.operationStatus = "FAILED";
    }
}