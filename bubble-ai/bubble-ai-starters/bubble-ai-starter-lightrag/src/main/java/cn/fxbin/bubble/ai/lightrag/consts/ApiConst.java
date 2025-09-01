package cn.fxbin.bubble.ai.lightrag.consts;

/**
 * ApiConst
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 16:08
 */
public final class ApiConst {

    private ApiConst() {
        // 工具类，不允许实例化
    }

    // Base URLs
    public static final String BASE_URL = "${lightragBaseUrl}";

    /**
     * HTTP请求头常量
     */
    public static final class Headers {
        /** Accept header */
        public static final String ACCEPT_JSON = "Accept: application/json";
        /** User-Agent header */
        public static final String USER_AGENT = "User-Agent: DM-LightRAG-Client/1.0";
        /** Content-Type header */
        public static final String CONTENT_TYPE_JSON = "Content-Type: application/json";
        /** Content-Type header */
        public static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "Content-Type: multipart/form-data";
        /** Content-Type header */
        public static final String CONTENT_TYPE_FORM_URLENCODED = "Content-Type: application/x-www-form-urlencoded";
        /** SSE Accept header */
        public static final String ACCEPT_EVENT_STREAM = "Accept: text/event-stream";
    }

    // API Endpoints - 根据 openapi.yaml 规范定义
    public static final class Endpoints {
        // Document endpoints
        public static final String DOCS_SCAN = "/documents/scan";
        public static final String DOCS_UPLOAD = "/documents/upload";
        public static final String DOCS_INSERT_TEXT = "/documents/text";
        public static final String DOCS_INSERT_TEXTS = "/documents/texts";
        public static final String DOCS_LIST = "/documents";
        public static final String DOCS_CLEAR = "/documents";
        public static final String DOCS_PIPELINE_STATUS = "/documents/pipeline_status";
        public static final String DOCS_DELETE_DOCUMENT = "/documents/delete_document";
        public static final String DOCS_CLEAR_CACHE = "/documents/clear_cache";
        public static final String DOCS_DELETE_ENTITY = "/documents/delete_entity";
        public static final String DOCS_DELETE_RELATION = "/documents/delete_relation";
        public static final String DOCS_TRACK_STATUS = "/documents/track_status/{track_id}";
        public static final String DOCS_PAGINATED = "/documents/paginated";
        public static final String DOCS_STATUS_COUNTS = "/documents/status_counts";

        // Query endpoints
        public static final String QUERY = "/query";
        public static final String QUERY_STREAM = "/query/stream";

        // Graph endpoints
        public static final String GRAPH_LABELS = "/graph/label/list";
        public static final String GRAPHS = "/graphs";
        public static final String GRAPH_ENTITY_EXISTS = "/graph/entity/exists";
        public static final String GRAPH_ENTITY_EDIT = "/graph/entity/edit";
        public static final String GRAPH_RELATION_EDIT = "/graph/relation/edit";

        // API endpoints (Language Model Services)
        public static final String API_VERSION = "/api/version";
        public static final String API_TAGS = "/api/tags";
        public static final String API_PS = "/api/ps";
        public static final String API_GENERATE = "/api/generate";
        public static final String API_CHAT = "/api/chat";

        // Default endpoints
        public static final String DEFAULT_HEALTH = "/health";
        public static final String DEFAULT_AUTH_STATUS = "/auth-status";
        public static final String DEFAULT_LOGIN = "/login";
    }

}
