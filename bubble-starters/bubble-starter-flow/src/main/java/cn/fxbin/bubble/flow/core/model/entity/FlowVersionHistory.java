package cn.fxbin.bubble.flow.core.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * FlowVersionHistory
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/18 10:40
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "flow_version_history", autoResultMap = true)
public class FlowVersionHistory extends BizEntity implements Serializable {

    @Schema(description = "历史版本ID")
    private Long id;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "版本描述")
    private String description;

    @Schema(description = "流程快照（节点+边完整数据）")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FlowChain snapshot;

    @Schema(description = "是否激活（非归档）")
    private Boolean active;

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
