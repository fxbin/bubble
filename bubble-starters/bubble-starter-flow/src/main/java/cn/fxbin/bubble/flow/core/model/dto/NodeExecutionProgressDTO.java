package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 节点执行进度DTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/07/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点执行进度信息")
public class NodeExecutionProgressDTO {

    @Schema(description = "节点执行日志ID")
    private Long nodeExecutionLogId;

    @Schema(description = "节点ID")
    private String nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型")
    private String nodeType;

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

}