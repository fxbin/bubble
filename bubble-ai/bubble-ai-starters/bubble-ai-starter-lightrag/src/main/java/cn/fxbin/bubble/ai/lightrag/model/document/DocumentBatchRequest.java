package cn.fxbin.bubble.ai.lightrag.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DocumentBatchRequest
 *
 * 批量上传文档请求参数
 * <pre>{@code
 * {
 *   "file_sources": [
 *     "First file source (optional)"
 *   ],
 *   "texts": [
 *     "This is the first text to be inserted.",
 *     "This is the second text to be inserted."
 *   ]
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:06
 */
@Data
public class DocumentBatchRequest implements Serializable {

    @JsonProperty("file_sources")
    private List<String> fileSources;

    private List<String> texts;
}
