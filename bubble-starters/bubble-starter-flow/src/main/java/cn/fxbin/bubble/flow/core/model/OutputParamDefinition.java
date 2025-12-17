package cn.fxbin.bubble.flow.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * OutputParamDefinition
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
 * @author fxbin
 * @version v1.0
 * @since 2025/4/18 16:05
 */
@Data
public class OutputParamDefinition implements Serializable {

    @Schema(description = "输出参数ID")
    private String id;

    /**
     * 节点内部输出参数名
     */
    @Schema(description = "节点内部输出参数名")
    private String name;

    /**
     * 映射到上下文的键名
     */
    @Schema(description = "映射到上下文的键名")
    private String target;

    /**
     * 输出类型（用于校验）
     */
    @Schema(description = "输出类型（用于校验）")
    private String type;

    /**
     * 是否必填
     */
    @Schema(description = "是否必填")
    private boolean required;

}