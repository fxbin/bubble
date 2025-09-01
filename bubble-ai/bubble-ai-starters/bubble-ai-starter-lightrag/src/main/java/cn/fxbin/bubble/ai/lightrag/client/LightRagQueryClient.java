package cn.fxbin.bubble.ai.lightrag.client;

import cn.fxbin.bubble.ai.lightrag.model.QueryResponse;
import cn.fxbin.bubble.ai.lightrag.model.query.QueryRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestSSE;

/**
 * LightRAG 知识查询客户端接口
 * 
 * <p>基于 Forest HTTP 客户端框架实现的 LightRAG 知识查询 API 接口。
 * 提供多种查询模式的知识检索功能，包括本地查询、全局查询、混合查询等，
 * 满足不同场景下的知识获取需求。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>智能问答：基于知识图谱的自然语言问答</li>
 *   <li>语义检索：基于向量相似度的语义搜索</li>
 *   <li>图谱推理：基于知识图谱的逻辑推理</li>
 *   <li>混合查询：结合多种查询策略的综合检索</li>
 *   <li>实时查询：支持流式和批量查询模式</li>
 * </ul>
 * 
 * <h3>查询模式说明：</h3>
 * <ul>
 *   <li><strong>naive</strong>: 朴素查询，基于关键词匹配</li>
 *   <li><strong>local</strong>: 本地查询，基于向量相似度检索</li>
 *   <li><strong>global</strong>: 全局查询，基于知识图谱推理</li>
 *   <li><strong>hybrid</strong>: 混合查询，结合本地和全局优势</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private LightRagQueryClient queryClient;
 * 
 * // 简单查询
 * QueryRequest request = QueryRequest.of("什么是人工智能？");
 * BaseResponse<QueryResponse> response = queryClient.query(request);
 * 
 * // 混合查询
 * QueryRequest hybridRequest = QueryRequest.builder()
 *     .query("人工智能在医疗领域的应用")
 *     .mode(QueryRequest.QueryMode.HYBRID)
 *     .maxResults(10)
 *     .includeSources(true)
 *     .build();
 * BaseResponse<QueryResponse> hybridResponse = queryClient.query(hybridRequest);
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
public interface LightRagQueryClient extends LightRagBaseClient {

    /**
     * 知识查询
     * 
     * <p>基于知识图谱的智能查询，支持多种查询模式：</p>
     * <ul>
     *   <li><strong>NAIVE</strong>：基于关键词的简单查询</li>
     *   <li><strong>LOCAL</strong>：基于局部子图的查询</li>
     *   <li><strong>GLOBAL</strong>：基于全局知识图谱的查询</li>
     *   <li><strong>HYBRID</strong>：混合查询模式，结合多种策略</li>
     * </ul>
     * 
     * <h4>查询流程：</h4>
     * <ol>
     *   <li>查询意图解析</li>
     *   <li>实体识别和链接</li>
     *   <li>子图构建和扩展</li>
     *   <li>答案生成和排序</li>
     *   <li>结果格式化和返回</li>
     * </ol>
     * 
     * @param request 查询请求，包含查询文本、查询模式、过滤条件等
     * @return 查询结果，包含相关实体、关系、摘要信息等
     */
    @Post("/query")
    QueryResponse query(@JSONBody QueryRequest request);

    /**
     * 流式查询（SSE）
     * 
     * <p>执行流式查询，通过 Server-Sent Events (SSE) 实时返回查询结果。
     * 适用于长时间查询或需要实时反馈的场景。</p>
     * 
     * <h4>SSE 事件格式：</h4>
     * <ul>
     *   <li><strong>data</strong>：查询结果数据，JSON 格式</li>
     *   <li><strong>event</strong>：事件类型（progress, result, error, complete）</li>
     *   <li><strong>id</strong>：事件唯一标识</li>
     * </ul>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * ForestSSE sse = queryClient.streamQuery(request);
     * sse.setOnMessage(event -> {
     *     String eventType = event.event();
     *     String data = event.data();
     *     // 处理 SSE 事件
     * }).listen();
     * }</pre>
     * 
     * @param request 查询请求
     * @return SSE 控制器，用于监听流式查询事件
     */
    @Post("/query/stream")
    ForestSSE streamQuery(@JSONBody QueryRequest request);

}