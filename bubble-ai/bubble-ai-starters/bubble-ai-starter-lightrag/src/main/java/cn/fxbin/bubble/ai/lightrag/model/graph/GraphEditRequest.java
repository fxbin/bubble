package cn.fxbin.bubble.ai.lightrag.model.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * GraphEditRequest
 *
 *
 * <pre>{@code
 * {
 *   "entity_name": "string",
 *   "updated_data": {
 *     "additionalProp1": {}
 *   },
 *   "allow_rename": false
 * }
 * }</pre>
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 10:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphEditRequest implements Serializable {


    @JsonProperty("entity_name")
    private String entityName;

    @JsonProperty("updated_data")
    private Map<String, Object> updatedData;

    @JsonProperty("allow_rename")
    private boolean allowRename;


}
