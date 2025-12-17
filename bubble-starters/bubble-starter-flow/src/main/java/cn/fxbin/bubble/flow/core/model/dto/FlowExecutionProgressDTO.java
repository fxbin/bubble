package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程执行进度DTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/07/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程执行进度信息")
public class FlowExecutionProgressDTO {

    @Schema(description = "流程执行日志ID")
    private Long flowExecutionLogId;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "执行状态")
    private FlowExecStatus status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "持续时间（毫秒）")
    private Long durationMs;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "节点执行进度列表")
    private List<NodeExecutionProgressDTO> nodeProgressList;

}