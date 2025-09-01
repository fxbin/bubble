package cn.fxbin.bubble.ai.lightrag.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 文档插入请求模型
 * 
 * <p>用于向 LightRAG 系统插入文档的请求参数。支持插入单个文档或批量文档，
 * 文档内容会被自动处理并构建知识图谱。</p>
 * 
 * <h3>请求示例：</h3>
 * <pre>{@code
 * {
 *   "file_source": "Source of the text (optional)",
 *   "text": "This is a sample text to be inserted into the RAG system."
 * }
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Data
public class DocumentRequest implements Serializable {

    @JsonProperty("file_source")
    private String fileSource;

    @NotBlank(message = "文档内容不能为空")
    private String text;


}