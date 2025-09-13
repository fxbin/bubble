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
 * 文档插入操作响应模型
 * 
 * <p>专用于文档插入操作（/documents/text, /documents/texts, /documents/upload）的响应模型。
 * 提供对插入操作结果的类型安全访问。</p>
 * 
 * <h3>可能的状态值：</h3>
 * <ul>
 *   <li><strong>success：</strong> 操作成功完成</li>
 *   <li><strong>duplicated：</strong> 文件已存在（仅上传操作）</li>
 *   <li><strong>partial_success：</strong> 批量操作部分成功</li>
 *   <li><strong>failure：</strong> 操作失败</li>
 * </ul>
 * 
 * <h3>响应示例：</h3>
 * 
 * <h4>成功插入：</h4>
 * <pre>{@code
 * {
 *   "status": "success",
 *   "message": "Document inserted successfully"
 * }
 * }</pre>
 * 
 * <h4>上传重复：</h4>
 * <pre>{@code
 * {
 *   "status": "duplicated",
 *   "message": "File already exists"
 * }
 * }</pre>
 * 
 * <h4>批量部分成功：</h4>
 * <pre>{@code
 * {
 *   "status": "partial_success",
 *   "message": "3 of 5 documents inserted successfully"
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
public class DocumentInsertResponse extends BaseResponse implements Serializable {

    /**
     * 插入操作状态
     * <p>可能值："success", "duplicated", "partial_success", "failure"</p>
     */
    private String status;

    /**
     * 关于插入操作结果的描述性消息
     */
    private String message;

    @JsonProperty("track_id")
    private String trackId;

    /**
     * 获取状态枚举以进行类型安全处理
     * 
     * @return 状态枚举，如果 status 为 null 或未识别则返回 null
     */
    public DocumentOperationStatus getStatusEnum() {
        return DocumentOperationStatus.fromStatusValue(status);
    }

    /**
     * 检查插入操作是否完全成功
     * 
     * @return 如果操作成功则返回 true
     */
    public boolean isSuccess() {
        return DocumentOperationStatus.SUCCESS.getStatusValue().equals(status);
    }

    /**
     * 检查文件是否重复（仅上传操作）
     * 
     * @return 如果文件已存在则返回 true
     */
    public boolean isDuplicated() {
        return DocumentOperationStatus.DUPLICATED.getStatusValue().equals(status);
    }

    /**
     * 检查操作是否部分成功（批量操作）
     * 
     * @return 如果操作部分成功则返回 true
     */
    public boolean isPartialSuccess() {
        return DocumentOperationStatus.PARTIAL_SUCCESS.getStatusValue().equals(status);
    }

    /**
     * 检查操作是否失败
     * 
     * @return 如果操作失败则返回 true
     */
    public boolean isFailure() {
        return DocumentOperationStatus.FAILURE.getStatusValue().equals(status);
    }
}