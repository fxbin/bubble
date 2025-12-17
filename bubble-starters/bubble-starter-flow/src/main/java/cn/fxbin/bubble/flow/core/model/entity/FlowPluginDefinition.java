package cn.fxbin.bubble.flow.core.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import cn.fxbin.bubble.flow.core.enums.PluginType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * FlowPluginDefinition
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 10:47
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程插件实体")
public class FlowPluginDefinition implements Serializable {

    @Schema(description = "插件ID")
    private String id;

    @Schema(description = "插件名称")
    private String name;

    @Schema(description = "插件版本")
    private Integer version;

    @Schema(description = "插件类型")
    private PluginType type;

    @Schema(description = "插件描述")
    private String description;

    @Schema(description = "插件配置元数据")
    private String metadata;

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
