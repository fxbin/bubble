package cn.fxbin.bubble.ai.lightrag.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DocumentsPaginatedResponse
 * 
 * <pre>{@code
 * {
 *   "documents": [
 *     {
 *       "id": "doc-1906a3e67b9af1c16faae35e0a6a285c",
 *       "content_summary": "CAGE Code 81205 \n \n   DIGITAL FLIGHT DATA ACQUISITION UNIT            \n737-600/-700/-700C/-800/-900                                        \nDATA FRAME INTERFACE CONTROL AND \nREQUIREMENTS DOCUMENT                                       \nDOCUMENT NUMBER...",
 *       "content_length": 1742074,
 *       "status": "processed",
 *       "created_at": "2025-08-22T08:13:57.167150+00:00",
 *       "updated_at": "2025-08-22T10:38:47.097020+00:00",
 *       "track_id": "upload_20250822_081343_2a84eac9",
 *       "chunks_count": 772,
 *       "error_msg": null,
 *       "metadata": {
 *         "processing_end_time": 1755859127,
 *         "processing_start_time": 1755856049
 *       },
 *       "file_path": "DIGITAL FLIGHT DATA ACQUISITION UNIT 737-600_-700_-700C_-800_-900 DATA FRAME INTERFACE CONTROL AND REQUIREMENTS.pdf"
 *     }
 *   ],
 *   "pagination": {
 *     "page": 1,
 *     "page_size": 50,
 *     "total_count": 1,
 *     "total_pages": 1,
 *     "has_next": false,
 *     "has_prev": false
 *   },
 *   "status_counts": {
 *     "failed": 1,
 *     "processed": 1,
 *     "all": 2
 *   }
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 18:01
 */
@Data
public class DocumentsPaginatedResponse implements Serializable {

    private List<Document> documents;

    private Pagination pagination;

    private StatusCounts statusCounts;

    @Data
    public static class Document {

        /**
         * 文档 ID
         */
        private String id;

        /**
         * 文档内容摘要
         */
        @JsonProperty("content_summary")
        private String contentSummary;

        /**
         * 文档内容长度
         */
        @JsonProperty("content_length")
        private Integer contentLength;

        /**
         * 文档处理状态
         */
        private String status;

        /**
         * 文档创建时间
         */
        @JsonProperty("created_at")
        private String createdAt;

        /**
         * 文档更新时间
         */
        @JsonProperty("updated_at")
        private String updatedAt;

        /**
         * 文档上传任务 ID
         */
        @JsonProperty("track_id")
        private String trackId;

        /**
         * 文档分块数量
         */
        @JsonProperty("chunks_count")
        private Integer chunksCount;

        /**
         * 文档错误信息
         */
        @JsonProperty("error_msg")
        private String errorMsg;

        /**
         * 文档元数据
         */
        private Map<String, Object> metadata;

        /**
         * 文档文件路径
         */
        @JsonProperty("file_path")
        private String filePath;
    }

    @Data
    public static class Pagination {

        /**
         * 当前页码
         */
        private Integer page;

        /**
         * 每页数量
         */
        @JsonProperty("page_size")
        private Integer pageSize;

        /**
         * 总记录数
         */
        @JsonProperty("total_count")
        private Integer totalCount;

        /**
         * 总页数
         */
        @JsonProperty("total_pages")
        private Integer totalPages;

        /**
         * 是否有下一页
         */
        @JsonProperty("has_next")
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        @JsonProperty("has_prev")
        private Boolean hasPrev;

        /**
         * 下一页页码
         */
        @JsonProperty("next_page")
        private Integer nextPage;

        /**
         * 上一页页码
         */
        @JsonProperty("prev_page")
        private Integer prevPage;
    }


    @Data
    public static class StatusCounts {

        /**
         * 处理失败数量
         */
        private Integer failed;

        /**
         * 处理成功数量
         */
        private Integer processed;

        /**
         * 所有文档数量
         */
        private Integer all;
    }
}
