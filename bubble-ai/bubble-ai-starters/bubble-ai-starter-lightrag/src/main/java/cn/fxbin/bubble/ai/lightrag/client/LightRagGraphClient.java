package cn.fxbin.bubble.ai.lightrag.client;

import cn.fxbin.bubble.ai.lightrag.model.graph.GraphEditRequest;
import cn.fxbin.bubble.ai.lightrag.model.graph.GraphExistsResponse;
import cn.fxbin.bubble.ai.lightrag.model.graph.GraphRelationEditRequest;
import cn.fxbin.bubble.ai.lightrag.model.graph.GraphResponse;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Query;

import java.util.List;
import java.util.Map;

/**
 * LightRAG 知识图谱管理客户端接口
 * 
 * <p>基于 Forest HTTP 客户端框架实现的 LightRAG 知识图谱管理 API 接口。
 * 提供知识图谱的创建、查询、更新、删除、导入导出、优化等全生命周期管理功能，
 * 支持大规模知识图谱的高效管理和维护。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>图谱构建：支持从多种数据源构建知识图谱</li>
 *   <li>图谱查询：提供灵活的图谱结构查询能力</li>
 *   <li>图谱更新：支持增量更新和批量修改</li>
 *   <li>图谱导入导出：支持多种格式的数据交换</li>
 *   <li>图谱优化：提供性能优化和存储压缩功能</li>
 *   <li>图谱分析：提供统计分析和可视化支持</li>
 * </ul>
 * 
 * <h3>数据格式支持：</h3>
 * <ul>
 *   <li><strong>JSON</strong>: 标准 JSON 格式，易于处理和传输</li>
 *   <li><strong>RDF</strong>: 资源描述框架，语义网标准格式</li>
 *   <li><strong>CSV</strong>: 逗号分隔值，适合表格数据</li>
 *   <li><strong>GraphML</strong>: 图形标记语言，支持复杂图结构</li>
 *   <li><strong>Cypher</strong>: Neo4j 查询语言格式</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private LightRagGraphClient graphClient;
 * 
 * // 获取图谱统计信息
 * BaseResponse<GraphResponse> stats = graphClient.getGraphStatistics();
 * 
 * // 导出图谱数据
 * GraphRequest exportRequest = GraphRequest.builder()
 *     .operation(GraphRequest.GraphOperation.EXPORT)
 *     .dataFormat(GraphRequest.DataFormat.JSON)
 *     .includeMetadata(true)
 *     .build();
 * BaseResponse<GraphResponse> exportResponse = graphClient.exportGraph(exportRequest);
 * 
 * // 优化图谱性能
 * GraphRequest optimizeRequest = GraphRequest.builder()
 *     .operation(GraphRequest.GraphOperation.OPTIMIZE)
 *     .optimizeOptions(Map.of("type", "index", "rebuild", true))
 *     .build();
 * BaseResponse<GraphResponse> optimizeResponse = graphClient.optimizeGraph(optimizeRequest);
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
public interface LightRagGraphClient extends LightRagBaseClient {

    /**
     * 获取图标签列表
     *
     * <p>获取所有可用的图标签。</p>
     *
     * @return 标签列表
     */
    @Get("/graph/label/list")
    List<String> getGraphLabels();

    /**
     * 获取知识图谱
     * 
     * <p>获取指定标签的知识图谱子图。</p>
     * 
     * @param label 标签名称
     * @param maxDepth 最大深度
     * @param maxNodes 最大节点数
     * @return 知识图谱数据
     */
    @Get("/graphs")
    GraphResponse getKnowledgeGraph(
            @Query("label") String label,
            @Query("max_depth") Integer maxDepth,
            @Query("max_nodes") Integer maxNodes
    );


    /**
     * 检查实体是否存在
     * 
     * <p>检查指定名称的实体是否存在。</p>
     * 
     * @param name 实体名称
     * @return 存在性检查结果
     */
    @Get("/graph/entity/exists")
    GraphExistsResponse checkEntityExists(@Query("name") String name);

    /**
     * 更新实体
     * 
     * <p>更新实体的属性信息。</p>
     * 
     * @param request 更新请求
     * @return 更新结果
     */
    @Post("/graph/entity/edit")
    Map<String, Object> updateEntity(@JSONBody GraphEditRequest request);

    /**
     * 更新关系
     * 
     * <p>更新关系的属性信息。</p>
     * 
     * @param request 更新请求
     * @return 更新结果
     */
    @Post("/graph/relation/edit")
    Map<String, Object> updateRelation(@JSONBody GraphRelationEditRequest request);

}