package cn.fxbin.bubble.ai.lightrag.model.document;

import cn.fxbin.bubble.ai.lightrag.model.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DocumentTrackResponse
 *
 * <pre>{@code
 * {
 *   "documents": [
 *     {
 *       "chunks_count": 12,
 *       "content_length": 15240,
 *       "content_summary": "Research paper on machine learning",
 *       "created_at": "2025-03-31T12:34:56",
 *       "file_path": "research_paper.pdf",
 *       "id": "doc_123456",
 *       "metadata": {
 *         "author": "John Doe",
 *         "year": 2025
 *       },
 *       "status": "PROCESSED",
 *       "track_id": "upload_20250729_170612_abc123",
 *       "updated_at": "2025-03-31T12:35:30"
 *     }
 *   ],
 *   "status_summary": {
 *     "PROCESSED": 1
 *   },
 *   "total_count": 1,
 *   "track_id": "upload_20250729_170612_abc123"
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentTrackResponse extends BaseResponse implements Serializable {

    private List<Document> documents;

    @JsonProperty("status_summary")
    private Map<String, Integer> statusSummary;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("track_id")
    private String trackId;

    @Data
    public static class Document {

        @JsonProperty("chunks_count")
        private Integer chunksCount;

        @JsonProperty("content_length")
        private Integer contentLength;

        @JsonProperty("content_summary")
        private String contentSummary;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("file_path")
        private String filePath;

        private String id;

        private Map<String, String> metadata;

        private String status;

        @JsonProperty("track_id")
        private String trackId;

        @JsonProperty("updated_at")
        private String updatedAt;

    }


}
