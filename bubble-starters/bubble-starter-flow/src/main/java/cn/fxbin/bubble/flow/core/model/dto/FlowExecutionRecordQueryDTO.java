package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.core.dataobject.PageRequest;
import cn.fxbin.bubble.flow.core.enums.FlowCallType;
import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * FlowExecutionRecordQueryDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/3 14:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowExecutionRecordQueryDTO extends PageRequest implements Serializable {

    @Schema(description = "流程id")
    private Long flowId;

    @Schema(description = "流程id", hidden = true)
    private List<Long> flowIds = Lists.newArrayList();

    @Schema(description = "流程版本")
    private Long version;

    @Schema(description = "执行状态")
    private FlowExecStatus execStatus;

    @Schema(description = "调用方式")
    private FlowCallType callType;

    @Schema(description = "执行开始时间")
    private Long startTime;

    @Schema(description = "执行结束时间")
    private Long endTime;


}
