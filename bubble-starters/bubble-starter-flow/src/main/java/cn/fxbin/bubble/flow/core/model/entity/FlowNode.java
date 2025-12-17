package cn.fxbin.bubble.flow.core.model.entity;

import cn.fxbin.bubble.core.constant.StringPool;
import cn.fxbin.bubble.flow.core.enums.PluginType;
import cn.fxbin.bubble.flow.core.model.InputParamDefinition;
import cn.fxbin.bubble.flow.core.model.OutputParamDefinition;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * FlowNode
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 10:47
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程节点实体")
@TableName(value = "flow_node", autoResultMap = true)
public class FlowNode extends BizEntity implements Serializable {

    @JsonProperty("nodeId")
    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "所属流程ID")
    private Long flowId;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "语义类型")
    private String semanticType;

    @Schema(description = "节点类型")
    private PluginType nodeType;

    @Schema(description = "组件实现类名或服务标识(对应 LiteFlow 组件 ID)")
    private String component;

    /**
     * [
     *   {
     *     "name": "inputData",   // 参数名称
     *     "type": "string",      // 参数类型（string/number/boolean/object等）
     *     "required": true,      // 是否必填
     *     "source": {            // 参数来源（可选）
     *       "type": "ref",       // 来源类型（ref-引用其他节点/literal-字面量）
     *       "nodeId": "node1",   // 来源节点ID（当type=ref时）
     *       "param": "output"    // 来源参数名（当type=ref时）
     *     },
     *     "default": "defaultValue" // 默认值（可选）
     *   }
     * ]
     * 示例：将上下文中的 inputData 映射到节点的 inputData 参数：
     * [
     *   {
     *     "name": "apiUrl",
     *     "type": "string",
     *     "required": true,
     *     "source": {
     *       "type": "ref",
     *       "nodeId": "configNode",
     *       "param": "apiEndpoint"
     *     }
     *   }
     * ]
     */
    @Schema(description = "节点输入参数")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<InputParamDefinition> inputParams;

    /**
     * [
     *   {
     *     "name": "responseBody", // 节点内部输出参数名
     *     "target": "apiResult",  // 映射到上下文的键名
     *     "type": "object"        // 输出类型（用于校验）
     *   }
     * ]
     * 示例：将 HTTP 响应结果映射到上下文中的 apiResult 字段：
     * [
     *   {
     *     "name": "body",
     *     "target": "apiResult",
     *     "type": "json"
     *   }
     * ]
     */
    @Schema(description = "节点输出参数")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<OutputParamDefinition> outputParams;

    @Schema(description = "动态配置（HTTP/特征计算等专属字段）")
    private String config;

    @Schema(description = "画布元数据")
    private String canvasMetadata;

    @Schema(description = "关联的插件ID（如果有的话）")
    private String pluginId;

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


    @JsonIgnore
    public String getPosition() {
        try {
            JSONObject jsonObject = JSONObject.parse(this.canvasMetadata);
            return jsonObject.getJSONObject("position").toJSONString();
        } catch (Exception e) {
            log.warn("读取节点 position 失败，返回空字符串");
            return StringPool.EMPTY;
        }
    }

    @JsonIgnore
    @TableField(exist = false)
    @Schema(description = "上一批节点", hidden = true)
    private final List<WeakReference<FlowNode>> pre = Lists.newArrayList();

    @JsonIgnore
    @TableField(exist = false)
    @Schema(description = "下一批节点", hidden = true)
    private final List<WeakReference<FlowNode>> next = Lists.newArrayList();

    public void addNextNode(FlowNode node) {
        next.add(new WeakReference<>(node));
    }

    public void addPreNode(FlowNode node) {
        pre.add(new WeakReference<>(node));
    }


}
