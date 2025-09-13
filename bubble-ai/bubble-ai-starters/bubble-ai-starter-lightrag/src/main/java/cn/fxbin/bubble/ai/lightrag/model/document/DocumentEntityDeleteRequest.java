package cn.fxbin.bubble.ai.lightrag.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * DocumentEntityDeleteRequest
 *
 * 文档实体删除请求
 * <pre>{@code
 * {
 *   "entity_name": "string"
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:36
 */
@Data
@Builder
public class DocumentEntityDeleteRequest implements Serializable {

    @JsonProperty("entity_name")
    private String entityName;

}
