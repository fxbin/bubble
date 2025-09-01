package cn.fxbin.bubble.ai.lightrag.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * DocumentRelationDeleteRequest
 * <pre>{@code
 * {
 *   "source_entity": "string",
 *   "target_entity": "string"
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:38
 */
@Data
public class DocumentRelationDeleteRequest implements Serializable {

    /**
     * 源实体
     */
    @JsonProperty("source_entity")
    private String sourceEntity;

    /**
     * 目标实体
     */
    @JsonProperty("target_entity")
    private String targetEntity;


}


