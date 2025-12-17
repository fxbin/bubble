package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.core.dataobject.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * FlowDefinitionQueryDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/22 11:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowDefinitionQueryDTO extends PageRequest {

    @Schema(description = "流程名称")
    private String name;

    private List<Long> flowIds;


}
