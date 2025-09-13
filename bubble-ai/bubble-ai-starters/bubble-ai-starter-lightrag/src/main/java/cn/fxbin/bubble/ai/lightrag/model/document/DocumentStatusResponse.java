package cn.fxbin.bubble.ai.lightrag.model.document;

import cn.fxbin.bubble.ai.lightrag.model.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DocumentStatusResponse
 *
 * <p>示例数据：
 * <pre>{@code
 * {
 *   "statuses": {
 *     "processed": [
 *       {
 *         "id": "doc-1906a3e67b9af1c16faae35e0a6a285c",
 *         "content_summary": "CAGE Code 81205 \n \n   DIGITAL FLIGHT DATA ACQUISITION UNIT            \n737-600/-700/-700C/-800/-900                                        \nDATA FRAME INTERFACE CONTROL AND \nREQUIREMENTS DOCUMENT                                       \nDOCUMENT NUMBER...",
 *         "content_length": 1742074,
 *         "status": "processed",
 *         "created_at": "2025-08-22T08:13:57.167150+00:00",
 *         "updated_at": "2025-08-22T10:38:47.097020+00:00",
 *         "track_id": "upload_20250822_081343_2a84eac9",
 *         "chunks_count": 772,
 *         "error_msg": null,
 *         "metadata": {
 *           "processing_end_time": 1755859127,
 *           "processing_start_time": 1755856049
 *         },
 *         "file_path": "DIGITAL FLIGHT DATA ACQUISITION UNIT 737-600_-700_-700C_-800_-900 DATA FRAME INTERFACE CONTROL AND REQUIREMENTS.pdf"
 *       }
 *     ],
 *     "failed": [
 *       {
 *         "id": "doc-80e35a467c44a5cff781d57b05e1b148",
 *         "content_summary": "XiY an-SQL：⼀种多⽣成器集成的T ext-to-SQL框                                                                                                                                                                         \n\n            架                                 ...",
 *         "content_length": 96661,
 *         "status": "failed",
 *         "created_at": "2025-08-27T07:51:36.827792+00:00",
 *         "updated_at": "2025-08-27T08:50:13.038647+00:00",
 *         "track_id": "insert_20250827_075136_d6deae04",
 *         "chunks_count": -1,
 *         "error_msg": "Error code: 400 - {'error': {'code': 'data_inspection_failed', 'param': None, 'message': 'Input data may contain inappropriate content.', 'type': 'data_inspection_failed'}, 'id': 'chatcmpl-9284916d-e92e-95b0-ab45-282292f0bccc', 'request_id': '9284916d-e92e-95b0-ab45-282292f0bccc'}",
 *         "metadata": {
 *           "processing_end_time": 1756284613,
 *           "processing_start_time": 1756284610
 *         },
 *         "file_path": "no-file-path"
 *       }
 *     ]
 *   }
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentStatusResponse extends BaseResponse implements Serializable {

    /**
     * 状态列表
     */
    private DocumentStatuses statuses;

    @Data
    public static class DocumentStatuses implements Serializable {
        private List<DocumentStatus> processed;
        private List<DocumentStatus> failed;
    }

    @Data
    public static class DocumentStatus implements Serializable {
        private String id;

        @JsonProperty("content_summary")
        private String contentSummary;

        @JsonProperty("content_length")
        private int contentLength;
        private String status;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("track_id")
        private String trackId;

        @JsonProperty("chunks_count")
        private int chunksCount;

        @JsonProperty("error_msg")
        private String errorMsg;

        private Map<String, Object> metadata;

        @JsonProperty("file_path")
        private String filePath;
    }

}
