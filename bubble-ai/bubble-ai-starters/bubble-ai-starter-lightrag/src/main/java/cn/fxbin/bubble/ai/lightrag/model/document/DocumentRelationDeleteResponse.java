package cn.fxbin.bubble.ai.lightrag.model.document;

import cn.fxbin.bubble.ai.lightrag.model.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除操作结果模型
 * 
 * <p>用于表示删除操作（实体、关系等）的结果。与 OpenAPI 规范保持一致。</p>
 * 
 * <h3>响应示例：</h3>
 * <pre>{@code
 * {
 *   "status": "success",
 *   "doc_id": "doc_123456",
 *   "message": "删除操作成功完成",
 *   "status_code": 200,
 *   "file_path": "/path/to/file.txt"
 * }
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRelationDeleteResponse extends BaseResponse implements Serializable {

    /**
     * 删除操作状态
     */
    private Status status;

    /**
     * 文档ID或实体ID
     */
    @JsonProperty("doc_id")
    private String docId;

    /**
     * 操作结果消息
     */
    private String message;

    /**
     * 状态码
     */
    @JsonProperty("status_code")
    @Builder.Default
    private Integer statusCode = 200;

    /**
     * 文件路径（如果删除了物理文件）
     */
    @JsonProperty("file_path")
    private String filePath;

    /**
     * 删除状态枚举
     */
    public enum Status {
        /**
         * 成功
         */
        @JsonProperty("success")
        SUCCESS,

        /**
         * 未找到
         */
        @JsonProperty("not_found")
        NOT_FOUND,

        /**
         * 失败
         */
        @JsonProperty("fail")
        FAIL
    }

    /**
     * 检查是否成功
     * 
     * @return true: 成功，false: 失败
     */
    public boolean isSuccess() {
        return Status.SUCCESS.equals(status);
    }

    /**
     * 检查是否未找到
     * 
     * @return true: 未找到，false: 找到了
     */
    public boolean isNotFound() {
        return Status.NOT_FOUND.equals(status);
    }

    /**
     * 检查是否失败
     * 
     * @return true: 失败，false: 成功
     */
    public boolean isFail() {
        return Status.FAIL.equals(status);
    }

    /**
     * 检查是否有文件路径
     * 
     * @return true: 有文件路径，false: 无文件路径
     */
    public boolean hasFilePath() {
        return filePath != null && !filePath.trim().isEmpty();
    }
}