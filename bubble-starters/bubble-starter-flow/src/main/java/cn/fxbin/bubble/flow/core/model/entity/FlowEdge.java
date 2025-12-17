package cn.fxbin.bubble.flow.core.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * FlowEdge
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 10:42
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程边实体")
public class FlowEdge extends BizEntity implements Serializable {

    @Schema(description = "边ID")
    private Long id;

    @Schema(description = "所属流程ID")
    private Long flowId;

    @Schema(description = "源节点ID")
    private String sourceNodeId;

    @Schema(description = "目标节点ID")
    private String targetNodeId;

    @Schema(description = "并行组")
    private String parallelGroup;

    @Schema(description = "条件分支表达式（如 value > 100）")
    private String conditionExpression;

    @Schema(description = "参数映射规则（字段级转换逻辑）")
    private String dataMapping;

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
}
