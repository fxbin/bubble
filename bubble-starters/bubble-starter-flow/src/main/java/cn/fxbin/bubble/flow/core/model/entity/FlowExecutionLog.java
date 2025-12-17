package cn.fxbin.bubble.flow.core.model.entity;

import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作流执行日志实体
 * <p>用于记录整个工作流（流程实例）的执行情况。</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024-07-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName(value = "flow_execution_log",  autoResultMap = true)
public class FlowExecutionLog extends BizEntity implements Serializable {

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 流程ID
     */
    @Schema(description = "流程ID")
    private Long flowId;

    /**
     * 流程实例ID (LiteFlow的ChainId)
     */
    @Schema(description = "流程实例ID")
    private String flowInstanceId;

    /**
     * 流程定义ID/名称 (例如：LiteFlow的ChainName)
     */
    @Schema(description = "流程定义ID/名称")
    private String flowDefinitionId;

    /**
     * 流程版本号
     * <p>记录执行时使用的流程版本，用于版本隔离和历史追溯</p>
     */
    @Schema(description = "流程版本号")
    private Integer flowVersion;

    /**
     * 数据集ID列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "数据集ID列表")
    private List<Long> datasetIds;

    /**
     * 执行条件 Json字符串
     * <p>记录触发该执行的条件，例如参数、环境等，便
     * PS: Ameco 项目中这里直接存
     * ALTER TABLE `ameco`.`flow_execution_log`
     * ADD COLUMN `condition` json NULL COMMENT '执行条件' AFTER `dataset_ids`;
     */
    @Schema(description = "执行条件")
    @TableField(value = "`condition`")
    private String condition;

    /**
     * 触发类型 (手动, 定时等)
     */
    @Schema(description = "触发类型（手动, 定时等）")
    private String triggerType;

    /**
     * 触发者/系统
     */
    @Schema(description = "触发者/系统")
    private String triggerBy;

    /**
     * 开始执行时间
     */
    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;

    /**
     * 结束执行时间
     */
    @Schema(description = "结束执行时间")
    private LocalDateTime endTime;

    /**
     * 总耗时 (毫秒)
     */
    @Schema(description = "总耗时 (毫秒)")
    private Long durationMs;

    /**
     * 执行状态 (RUNNING, SUCCESS, FAIL, CANCELLED)
     * @see FlowExecStatus
     */
    @Schema(description = "执行状态")
    @TableField(value = "`status`")
    private FlowExecStatus status;

    /**
     * 错误信息 (如果执行失败)
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 输入参数 (JSON格式)
     */
    @Schema(description = "输入参数")
    private String inputParameters;

    /**
     * 输出结果 (JSON格式)
     */
    @Schema(description = "输出结果")
    private String outputResults;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

}