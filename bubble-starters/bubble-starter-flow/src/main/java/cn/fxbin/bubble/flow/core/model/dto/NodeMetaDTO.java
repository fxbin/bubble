package cn.fxbin.bubble.flow.core.model.dto;

import cn.fxbin.bubble.flow.core.enums.PluginType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * NodeMetaDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeMetaDTO implements Serializable {


    /**
     * 节点名称
     * 在可视化界面中显示的节点名称
     */
    private String name;

    /**
     * 节点分类
     * 用于在节点面板中对节点进行分组展示
     */
    private PluginType category;

    /**
     * 节点描述
     * 对节点功能的详细说明
     */
    private String desc;

    /**
     * 输入数据格式
     * 用于描述节点的输入数据结构
     */
    private JsonNode inputSchema;

    /**
     * 输出数据格式
     * 用于描述节点的输出数据结构
     */
    private JsonNode outputSchema;


}
