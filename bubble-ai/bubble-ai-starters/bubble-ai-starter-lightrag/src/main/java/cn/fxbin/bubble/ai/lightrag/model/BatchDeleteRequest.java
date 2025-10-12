package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求模型
 * 
 * <h3>请求示例：</h3>
 * <pre>{@code
 * {
 *   "doc_ids": [
 *     "string"
 *   ],
 *   "delete_file": false
 * }
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteRequest implements Serializable {

    @NotEmpty(message = "文档ID列表不能为空")
    @JsonProperty("doc_ids")
    private List<String> docIds;

    @JsonProperty("delete_file")
    private Boolean deleteFile;


}