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
 * 文档操作响应模型
 * 
 * <p>用于文档操作的响应模型，包含操作状态、进度信息和错误详情。
 * 不同的接口返回不同的状态值 - 使用提供的枚举保证类型安全。</p>
 * 
 * <h3>各接口的状态值：</h3>
 * <ul>
 *   <li><strong>/documents/scan：</strong> {@code "scanning_started"}</li>
 *   <li><strong>/documents/upload：</strong> {@code "success"}, {@code "duplicated"}, {@code "failure"}</li>
 *   <li><strong>/documents/text：</strong> {@code "success"}, {@code "failure"}</li>
 *   <li><strong>/documents/texts：</strong> {@code "success"}, {@code "partial_success"}, {@code "failure"}</li>
 *   <li><strong>DELETE /documents：</strong> {@code "success"}, {@code "partial_success"}, {@code "busy"}, {@code "fail"}</li>
 *   <li><strong>/documents/delete_document：</strong> {@code "deletion_started"}, {@code "busy"}, {@code "not_allowed"}</li>
 *   <li><strong>/documents/clear_cache：</strong> {@code "success"}, {@code "fail"}</li>
 *   <li><strong>/documents/delete_entity：</strong> {@code "success"}, {@code "not_found"}, {@code "fail"}</li>
 *   <li><strong>/documents/delete_relation：</strong> {@code "success"}, {@code "not_found"}, {@code "fail"}</li>
 * </ul>
 * 
 * <h3>响应示例：</h3>
 * 
 * <h4>扫描操作：</h4>
 * <pre>{@code
 * {
 *   "status": "scanning_started",
 *   "message": "Scanning process has been initiated in the background",
 *   "track_id": "scan_20250827_084956_27a64e73"
 * }
 * }</pre>
 * 
 * <h4>插入操作：</h4>
 * <pre>{@code
 * {
 *   "status": "success",
 *   "message": "Document inserted successfully"
 * }
 * }</pre>
 * 
 * <h4>删除操作：</h4>
 * <pre>{@code
 * {
 *   "status": "deletion_started",
 *   "message": "Document deletion started in background",
 *   "track_id": "delete_20250828_123456_abc123",
 *   "doc_id": "document_123"
 * }
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse extends BaseResponse implements Serializable {

    /**
     * 操作状态字符串值
     * <p>不同的接口返回不同的状态值。使用 {@link #getStatusEnum()} 进行类型安全的访问。</p>
     * 
     * @see DocumentOperationStatus
     */
    private String status;

    /**
     * 操作结果的描述性消息
     */
    private String message;

    /**
     * 异步操作的跟踪ID
     * <p>针对在后台运行的操作（扫描、删除）</p>
     */
    @JsonProperty("track_id")
    private String trackId;

    /**
     * 文档ID
     * <p>删除文档时返回，或当操作影响特定文档时返回</p>
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
     * 检查操作是否成功
     * 
     * @return 如果操作成功则返回 true
     */
    public boolean isSuccess() {
        DocumentOperationStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.isSuccess();
    }

    /**
     * 检查操作是否部分成功
     * 
     * @return 如果操作部分成功则返回 true
     */
    public boolean isPartialSuccess() {
        DocumentOperationStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.isPartialSuccess();
    }

    /**
     * 检查操作是否失败
     * 
     * @return 如果操作失败则返回 true
     */
    public boolean isFailure() {
        DocumentOperationStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.isFailure();
    }

    /**
     * 检查操作是否正在进行
     * 
     * @return 如果操作正在进行则返回 true
     */
    public boolean isInProgress() {
        DocumentOperationStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.isInProgress();
    }

}