package cn.fxbin.bubble.flow.core.model;

import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.fxbin.bubble.flow.core.util.FlowUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * FlowSaveDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/22 10:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowChain implements Serializable {

    private List<FlowNode> nodes;

    private List<FlowEdge> edges;

    @JsonIgnore
    public void rebuild() {
        FlowUtils.buildNodeRelation(this);
    }

}
