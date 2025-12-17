package cn.fxbin.bubble.flow.core.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点执行日志实体
 * <p>用于记录工作流中单个节点的执行详情。</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024-07-31
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FlowNodeExecutionLog extends BizEntity implements Serializable {

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工作流执行日志ID (flow_execution_log.id)
     */
    private Long flowExecutionLogId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称 (LiteFlow的NodeName, 通常是组件类名)
     */
    private String nodeName;

    /**
     * 节点类型 (例如：LiteFlow的NodeType, 或者自定义的业务节点类型)
     */
    private String nodeType;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;

    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时 (毫秒)
     */
    private Long durationMs;

    /**
     * 执行状态 (SUCCESS, FAIL)
     * @see FlowExecStatus
     */
    private FlowExecStatus status;

    /**
     * 输入数据/参数 (JSON格式，可选)
     */
    private String inputData;

    /**
     * 输出数据/结果 (JSON格式，可选)
     */
    private String outputData;

    /**
     * 错误信息 (如果执行失败)
     */
    private String errorMessage;

    /**
     * 详细错误堆栈 (如果执行失败)
     */
    private String errorStackTrace;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

}