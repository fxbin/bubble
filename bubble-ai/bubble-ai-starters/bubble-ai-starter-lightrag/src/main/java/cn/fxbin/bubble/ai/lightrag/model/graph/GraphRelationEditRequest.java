package cn.fxbin.bubble.ai.lightrag.model.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * GraphRelationEditRequest
 *
 * <pre>{@code
 * {
 *   "source_id": "string",
 *   "target_id": "string",
 *   "updated_data": {
 *     "additionalProp1": {}
 *   }
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 10:07
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class GraphRelationEditRequest implements Serializable {

    @JsonProperty("source_id")
    private String sourceId;

    @JsonProperty("target_id")
    private String targetId;

    @JsonProperty("updated_data")
    private Map<String, Object> updatedData;

}
