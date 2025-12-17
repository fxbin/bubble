package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.flow.core.enums.FlowPublishStatus;
import cn.fxbin.bubble.flow.core.enums.FlowType;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * FlowDefinitionDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/22 10:56
 */
@Data
public class FlowDefinitionDTO implements Serializable {

    /**
     * 流程ID
     */
    private Long flowId;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 流程版本
     */
    private Integer version;

    /**
     * 工作流类型（0-探索平台,1-训练平台）
     */
    @Schema(description = "工作流类型（0-探索平台,1-训练平台）")
    private FlowType type;

    /**
     * 工作流表达式
     */
    private String el;

    /**
     * 工作流链
     */
    private FlowChain schema;

    /**
     * 工作流状态
     */
    @Schema(description = "工作流状态: 1.草稿；2.已发布 ; 默认草稿", requiredMode = Schema.RequiredMode.REQUIRED, hidden = true)
    private FlowPublishStatus status = FlowPublishStatus.DRAFT;

}
