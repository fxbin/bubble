package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * HealthCheckResponse
 *
 * <p>健康检查响应模型</p>
 *
 * <h1>示例数据</h1>
 * <pre>
 * {
 *   "status": "healthy",
 *   "working_directory": "/app/data/rag_storage",
 *   "input_directory": "/app/data/inputs",
 *   "configuration": {
 *     "llm_binding": "openai",
 *     "llm_binding_host": "https://dashscope.aliyuncs.com/compatible-mode/v1",
 *     "llm_model": "qwen3-30b-a3b-instruct-2507",
 *     "embedding_binding": "openai",
 *     "embedding_binding_host": "https://dashscope.aliyuncs.com/compatible-mode/v1",
 *     "embedding_model": "text-embedding-v4",
 *     "max_tokens": 10000,
 *     "kv_storage": "PGKVStorage",
 *     "doc_status_storage": "PGDocStatusStorage",
 *     "graph_storage": "PGGraphStorage",
 *     "vector_storage": "PGVectorStorage",
 *     "enable_llm_cache_for_extract": true,
 *     "enable_llm_cache": true,
 *     "workspace": "",
 *     "max_graph_nodes": 1000,
 *     "enable_rerank": true,
 *     "rerank_model": "gte-rerank-v2",
 *     "rerank_binding_host": "https://dashscope.aliyuncs.com/compatible-mode/v1",
 *     "summary_language": "English",
 *     "force_llm_summary_on_merge": 4,
 *     "max_parallel_insert": 3,
 *     "cosine_threshold": 0.2,
 *     "min_rerank_score": 0,
 *     "related_chunk_number": 5,
 *     "max_async": 10,
 *     "embedding_func_max_async": 8,
 *     "embedding_batch_num": 10
 *   },
 *   "auth_mode": "disabled",
 *   "pipeline_busy": false,
 *   "keyed_locks": {
 *     "process_id": 1,
 *     "cleanup_performed": {
 *       "mp_cleaned": 0,
 *       "async_cleaned": 0
 *     },
 *     "current_status": {
 *       "total_mp_locks": 0,
 *       "pending_mp_cleanup": 0,
 *       "total_async_locks": 0,
 *       "pending_async_cleanup": 0
 *     }
 *   },
 *   "core_version": "1.4.6",
 *   "api_version": "0198",
 *   "webui_title": null,
 *   "webui_description": null
 * }
 * </pre>
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 16:29
 */
@Data
public class HealthCheckResponse implements Serializable {

    private String status;

    @JsonProperty("working_directory")
    private String workingDirectory;

    @JsonProperty("input_directory")
    private String inputDirectory;

    private Configuration configuration;

    @JsonProperty("auth_mode")
    private String authMode;

    @JsonProperty("pipeline_busy")
    private Boolean pipelineBusy;

    @JsonProperty("keyed_locks")
    private KeyedLocks keyedLocks;

    @JsonProperty("core_version")
    private String coreVersion;

    @JsonProperty("api_version")
    private String apiVersion;

    @JsonProperty("webui_title")
    private String webuiTitle;

    @JsonProperty("webui_description")
    private String webuiDescription;

    @Data
    public static class KeyedLocks {
        @JsonProperty("process_id")
        private Integer processId;

        @JsonProperty("cleanup_performed")
        private CleanupPerformed cleanupPerformed;

        @JsonProperty
        private CurrentStatus currentStatus;

        @Data
        public static class CleanupPerformed {
            @JsonProperty("mp_cleaned")
            private Integer mpCleaned;

            @JsonProperty("async_cleaned")
            private Integer asyncCleaned;
        }

        @Data
        public static class CurrentStatus {
            @JsonProperty("total_mp_locks")
            private Integer totalMpLocks;

            @JsonProperty("pending_mp_cleanup")
            private Integer pendingMpCleanup;

            @JsonProperty("total_async_locks")
            private Integer totalAsyncLocks;

            @JsonProperty("pending_async_cleanup")
            private Integer pendingAsyncCleanup;
        }

    }

    @Data
    public static class Configuration {

        @JsonProperty("llm_binding")
        private String llmBinding;

        @JsonProperty("llm_binding_host")
        private String llmBindingHost;

        @JsonProperty("llm_model")
        private String llmModel;

        @JsonProperty("embedding_binding")
        private String embeddingBinding;

        @JsonProperty("embedding_binding_host")
        private String embeddingBindingHost;

        @JsonProperty("embedding_model")
        private String embeddingModel;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        @JsonProperty("kv_storage")
        private String kvStorage;

        @JsonProperty("doc_status_storage")
        private String docStatusStorage;

        @JsonProperty("graph_storage")
        private String graphStorage;

        @JsonProperty("vector_storage")
        private String vectorStorage;

        @JsonProperty("enable_llm_cache_for_extract")
        private Boolean enableLlmCacheForExtract;

        @JsonProperty("enable_llm_cache")
        private Boolean enableLlmCache;

        private String workspace;

        @JsonProperty("max_graph_nodes")
        private Integer maxGraphNodes;

        @JsonProperty("enable_rerank")
        private Boolean enableRerank;

        @JsonProperty("rerank_model")
        private String rerankModel;

        @JsonProperty("rerank_binding_host")
        private String rerankBindingHost;

        @JsonProperty("summary_language")
        private String summaryLanguage;

        @JsonProperty("force_llm_summary_on_merge")
        private Integer forceLlmSummaryOnMerge;

        @JsonProperty("max_parallel_insert")
        private Integer maxParallelInsert;

        @JsonProperty("cosine_threshold")
        private Double cosineThreshold;

        @JsonProperty("min_rerank_score")
        private Double minRerankScore;

        @JsonProperty("related_chunk_number")
        private Integer relatedChunkNumber;

        @JsonProperty("max_async")
        private Integer maxAsync;

        @JsonProperty("embedding_func_max_async")
        private Integer embeddingFuncMaxAsync;

        @JsonProperty("embedding_batch_num")
        private Integer embeddingBatchNum;

    }

}
