package cn.fxbin.bubble.flow.core.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import cn.fxbin.bubble.flow.core.enums.ArchiveStrategy;
import cn.fxbin.bubble.flow.core.model.dto.FlowArchiveConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程版本归档实体
 * 
 * @author fxbin
 * @since 2025-09-05 17:58
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_version_archive")
@Slf4j
public class FlowArchiveRecord extends BizEntity implements Serializable {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

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
     * 流程配置JSON
     */
    @Schema(description = "流程配置JSON")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FlowArchiveConfigDTO flowConfig;

    /**
     * 版本描述
     */
    @Schema(description = "版本描述")
    private String description;

    /**
     * 是否激活
     */
    @Schema(description = "是否激活")
    private Boolean active;

    /**
     * 归档时间
     */
    @Schema(description = "归档时间")
    private LocalDateTime archivedTime;

    /**
     * 归档原因
     */
    @Schema(description = "归档原因")
    private String archiveReason;

    /**
     * 归档策略
     */
    @Schema(description = "归档策略")
    private ArchiveStrategy archiveStrategy;


    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 从FlowVersionHistory创建归档记录
     */
    public static FlowArchiveRecord fromFlowVersionHistory(FlowVersionHistory versionHistory, String archiveStrategy, String archiveReason) {
        FlowArchiveRecord record = new FlowArchiveRecord();
        record.setFlowId(versionHistory.getFlowId());
        record.setVersion(versionHistory.getVersion());
        // 使用版本历史的快照数据作为流程配置
        record.setFlowConfig(FlowArchiveConfigDTO.builder()
                .strategy(ArchiveStrategy.valueOf(archiveStrategy))
                .build());
        record.setDescription(versionHistory.getDescription());
        // 归档后标记为非激活
        record.setActive(false);
        record.setArchivedTime(LocalDateTime.now());
        record.setArchiveReason(archiveReason);
        
        // 安全地设置归档策略
        try {
            record.setArchiveStrategy(ArchiveStrategy.valueOf(archiveStrategy));
        } catch (IllegalArgumentException e) {
            // 如果无法转换，使用默认策略
            record.setArchiveStrategy(ArchiveStrategy.BY_TIME);
            if (log != null) {
                log.warn("无法识别的归档策略: {}, 使用默认策略 BY_TIME", archiveStrategy);
            }
        }
        
        record.setCreateBy(versionHistory.getCreateBy());
        record.setUpdateBy(versionHistory.getUpdateBy());
        record.setCreateTime(versionHistory.getCreateTime());
        record.setUpdateTime(System.currentTimeMillis());
        return record;
    }
}