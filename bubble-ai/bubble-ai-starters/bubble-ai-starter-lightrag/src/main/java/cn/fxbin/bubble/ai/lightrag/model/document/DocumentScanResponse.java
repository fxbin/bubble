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
 * 文档扫描操作响应模型
 * 
 * <p>专用于文档扫描操作（/documents/scan）的响应模型。
 * 此操作会启动对输入目录中新文档的后台扫描。</p>
 * 
 * <h3>可能的状态值：</h3>
 * <ul>
 *   <li><strong>scanning_started：</strong> 扫描进程已在后台启动</li>
 * </ul>
 * 
 * <h3>响应示例：</h3>
 * <pre>{@code
 * {
 *   "status": "scanning_started",
 *   "message": "Scanning process has been initiated in the background",
 *   "track_id": "scan_20250827_084956_27a64e73"
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
public class DocumentScanResponse extends BaseResponse implements Serializable {

    /**
     * 扫描操作状态
     * <p>预期值："scanning_started"</p>
     */
    private String status;

    /**
     * 关于扫描操作的描述性消息
     */
    private String message;

    /**
     * 扫描操作的跟踪ID
     * <p>使用此 ID 通过 /documents/track_status/{track_id} 跟踪扫描进度</p>
     */
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
     * 检查扫描是否成功启动
     * 
     * @return 如果扫描已启动则返回 true
     */
    public boolean isScanStarted() {
        return DocumentOperationStatus.SCANNING_STARTED.getStatusValue().equals(status);
    }
}