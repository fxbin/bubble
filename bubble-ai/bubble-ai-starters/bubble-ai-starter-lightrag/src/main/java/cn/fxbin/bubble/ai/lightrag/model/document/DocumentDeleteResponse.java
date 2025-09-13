package cn.fxbin.bubble.ai.lightrag.model.document;

import cn.fxbin.bubble.ai.lightrag.model.BaseResponse;
import cn.fxbin.bubble.ai.lightrag.model.enums.DocumentOperationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文档删除操作响应模型
 * 
 * <p>专用于文档删除操作（/documents/delete_document, DELETE /documents）的响应模型。
 * 提供对删除操作结果的类型安全访问。</p>
 * 
 * <h3>/documents/delete_document 的可能状态值：</h3>
 * <ul>
 *   <li><strong>deletion_started：</strong> 文档删除已在后台启动</li>
 *   <li><strong>busy：</strong> 管道忙碌，无法执行操作</li>
 *   <li><strong>not_allowed：</strong> 操作不被允许（例如：实体提取 LLM 缓存被禁用时）</li>
 * </ul>
 * 
 * <h3>DELETE /documents 的可能状态值：</h3>
 * <ul>
 *   <li><strong>success：</strong> 所有文档成功清除</li>
 *   <li><strong>partial_success：</strong> 文档清除完成但有错误</li>
 *   <li><strong>busy：</strong> 管道忙碌，无法执行操作</li>
 *   <li><strong>fail：</strong> 所有存储删除操作失败</li>
 * </ul>
 * 
 * <h3>响应示例：</h3>
 * 
 * <h4>文档删除已启动：</h4>
 * <pre>{@code
 * {
 *   "status": "deletion_started",
 *   "message": "Document deletion started in background",
 *   "track_id": "delete_20250828_123456_abc123",
 *   "doc_id": "document_123"
 * }
 * }</pre>
 * 
 * <h4>管道忙碌：</h4>
 * <pre>{@code
 * {
 *   "status": "busy",
 *   "message": "Pipeline is currently busy processing other operations"
 * }
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDeleteResponse extends BaseResponse implements Serializable {

    /**
     * 删除操作状态
     * <p>可能值："deletion_started", "busy", "not_allowed", "success", "partial_success", "fail"</p>
     */
    private String status;

    /**
     * 关于删除操作结果的描述性消息
     */
    private String message;

    /**
     * 异步删除操作的跟踪ID
     * <p>当删除在后台启动时存在</p>
     */
    @JsonProperty("track_id")
    private String trackId;

    /**
     * 被删除的文档ID
     * <p>删除特定文档时存在</p>
     */
    @JsonProperty("doc_id")
    private String docId;

    /**
     * 获取状态枚举以进行类型安全处理
     * 
     * @return 状态枚举，如果 status 为 null 或未识别则返回 null
     */
    public DocumentOperationStatus getStatusEnum() {
        return DocumentOperationStatus.fromStatusValue(status);
    }

    /**
     * 检查删除操作是否成功启动
     * 
     * @return 如果删除已在后台启动则返回 true
     */
    public boolean isDeletionStarted() {
        return DocumentOperationStatus.DELETION_STARTED.getStatusValue().equals(status);
    }

    /**
     * 检查管道是否忙碌
     * 
     * @return 如果管道忙碌则返回 true
     */
    public boolean isBusy() {
        return DocumentOperationStatus.BUSY.getStatusValue().equals(status);
    }

    /**
     * 检查操作是否不被允许
     * 
     * @return 如果操作不被允许则返回 true
     */
    public boolean isNotAllowed() {
        return DocumentOperationStatus.NOT_ALLOWED.getStatusValue().equals(status);
    }

    /**
     * 检查是否所有文档都成功清除
     * 
     * @return 如果所有文档都被清除则返回 true
     */
    public boolean isAllCleared() {
        return DocumentOperationStatus.ALL_CLEARED.getStatusValue().equals(status);
    }

    /**
     * 检查清除是否部分成功
     * 
     * @return 如果清除部分成功则返回 true
     */
    public boolean isPartiallyCleared() {
        return DocumentOperationStatus.PARTIAL_CLEARED.getStatusValue().equals(status);
    }

    /**
     * 检查清除是否失败
     * 
     * @return 如果清除失败则返回 true
     */
    public boolean isClearFailed() {
        return DocumentOperationStatus.CLEAR_FAILED.getStatusValue().equals(status);
    }
}