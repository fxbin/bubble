package cn.fxbin.bubble.ai.lightrag.client;

import cn.fxbin.bubble.ai.lightrag.consts.ApiConst;
import cn.fxbin.bubble.ai.lightrag.model.BatchDeleteRequest;
import cn.fxbin.bubble.ai.lightrag.model.PageRequest;
import cn.fxbin.bubble.ai.lightrag.model.document.*;
import com.dtflys.forest.annotation.*;

import java.io.File;
import java.util.Map;

/**
 * LightRAG 文档管理客户端接口
 * 
 * <p>基于 Forest HTTP 客户端框架实现的 LightRAG 文档管理 API 接口。
 * 提供文档的插入、删除、查询、批量操作等功能，支持多种文档格式和处理模式。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>单文档插入：支持文本、PDF、Word 等格式</li>
 *   <li>批量文档插入：提高处理效率</li>
 *   <li>文档删除：支持单个和批量删除</li>
 *   <li>文档查询：获取文档详情和列表</li>
 *   <li>文档更新：修改文档内容和元数据</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private LightRagDocumentClient documentClient;
 * 
 * // 插入单个文档
 * DocumentRequest request = DocumentRequest.builder()
 *     .text("这是一篇关于人工智能的文章...")
 *     .description("AI技术介绍")
 *     .build();
 * BaseResponse<DocumentResponse> response = documentClient.insertDocument(request);
 * 
 * // 批量插入文档
 * List<DocumentRequest> documents = Arrays.asList(request1, request2, request3);
 * BaseResponse<List<DocumentResponse>> batchResponse = documentClient.insertDocumentsBatch(documents);
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Headers({
        ApiConst.Headers.ACCEPT_JSON
})
public interface LightRagDocumentClient extends LightRagBaseClient {

    /**
     * 扫描新文档
     *
     * <p>触发扫描输入目录中的新文档，并启动处理流程。
     * 如果扫描流程已在运行，则返回相应状态。</p>
     *
     * @return 扫描状态，包含跟踪ID和处理状态
     */
    @Post(ApiConst.Endpoints.DOCS_SCAN)
    @Headers({
            ApiConst.Headers.ACCEPT_JSON,
    })
    DocumentScanResponse scanDocuments();

    /**
     * 上传文件到输入目录
     *
     * <p>上传文件到输入目录并进行索引处理。
     * 支持多种文件格式，如PDF、Word、文本文件等。</p>
     *
     * @param file 文件
     * @return 上传结果，包含状态和处理信息
     */
    @Post(ApiConst.Endpoints.DOCS_UPLOAD)
    DocumentInsertResponse uploadDocument(@DataFile("file") File file);

    /**
     * 插入文本内容
     * 
     * <p>向 LightRAG 系统插入文本内容，系统会自动进行文本处理、
     * 实体抽取、关系识别等操作，并将结果存储到知识图谱中。</p>
     * 
     * <h4>处理流程：</h4>
     * <ol>
     *   <li>文本预处理和清洗</li>
     *   <li>实体识别和抽取</li>
     *   <li>关系识别和抽取</li>
     *   <li>知识图谱更新</li>
     *   <li>索引构建</li>
     * </ol>
     * 
     * @param request 文档插入请求，包含文档内容、描述、元数据等信息
     * @return 插入结果，包含文档ID、处理状态、统计信息等
     * @throws com.dtflys.forest.exceptions.ForestNetworkException 网络连接异常
     * @throws com.dtflys.forest.exceptions.ForestRuntimeException 请求处理异常
     */
    @Post(ApiConst.Endpoints.DOCS_INSERT_TEXT)
    DocumentInsertResponse insertText(@JSONBody DocumentRequest request);

    /**
     * 批量插入文本内容
     * 
     * <p>批量向 LightRAG 系统插入多个文本内容，提高处理效率。
     * 系统会并行处理多个文本，并返回每个文本的处理结果。</p>
     * 
     * <h4>批量处理优势：</h4>
     * <ul>
     *   <li>减少网络请求次数</li>
     *   <li>提高处理吞吐量</li>
     *   <li>优化资源利用</li>
     *   <li>支持事务性操作</li>
     * </ul>
     * 
     * @param request 文档批量插入请求，包含多个文档的插入信息
     * @return 批量插入结果，包含每个文档的处理状态和统计信息
     */
    @Post(ApiConst.Endpoints.DOCS_INSERT_TEXTS)
    DocumentInsertResponse insertTexts(@JSONBody DocumentBatchRequest request);


    /**
     * 清除所有文档
     *
     * <h1>慎用，会清除所有文档</h1>
     * <p>清除系统中的所有文档。</p>
     *
     * @return 清除操作结果
     */
    @Delete("/documents")
    DocumentDeleteResponse clearDocuments();


    /**
     * 获取所有文档
     *
     * <p>获取系统中的所有文档列表。</p>
     *
     * @return 文档列表
     */
    @Get("/documents")
    DocumentStatusResponse getDocuments();

    /**
     * 获取处理状态
     *
     * <p>获取处理管道状态信息。</p>
     *
     * @return 处理管道状态
     */
    @Get(ApiConst.Endpoints.DOCS_PIPELINE_STATUS)
    DocumentPipelineStatusResponse getPipelineStatus();


    /**
     * 删除指定文档
     * 
     * <p>根据文档ID删除指定的文档及其相关的知识图谱数据。
     * 删除操作会清理文档内容、相关实体、关系和索引信息。</p>
     * 
     * <h4>删除影响：</h4>
     * <ul>
     *   <li>删除文档原始内容</li>
     *   <li>清理孤立的实体和关系</li>
     *   <li>更新知识图谱结构</li>
     *   <li>重建相关索引</li>
     * </ul>
     * 
     * @param request 批量删除请求，包含文档ID列表和删除选项
     * @return 删除结果，包含操作状态和影响的数据统计
     */
    @Delete(ApiConst.Endpoints.DOCS_DELETE_DOCUMENT)
    DocumentDeleteResponse deleteDocuments(@JSONBody BatchDeleteRequest request);

    /**
     * 清除缓存
     *
     * <p>清除系统中的缓存数据。支持清除特定模式的缓存。</p>
     *
     * @param request 缓存清除请求，包含清除选项
     * @return 清除结果
     */
    @Post(ApiConst.Endpoints.DOCS_CLEAR_CACHE)
    DocumentResponse clearCache(@JSONBody DocumentClearCacheRequest request);

    /**
     * 删除实体
     *
     * <p>从知识图谱中删除指定的实体及其所有关系。</p>
     *
     * @param request 删除实体请求
     * @return 删除结果
     */
    @Delete(ApiConst.Endpoints.DOCS_DELETE_ENTITY)
    DocumentResponse deleteEntity(@JSONBody DocumentEntityDeleteRequest request);


    /**
     * 删除关系
     *
     * <p>从知识图谱中删除指定的关系。</p>
     *
     * @return 删除结果
     */
    @Delete(ApiConst.Endpoints.DOCS_DELETE_RELATION)
    DocumentRelationDeleteResponse deleteRelation(@JSONBody DocumentRelationDeleteRequest request);


    /**
     * 获取跟踪状态
     *
     * <p>根据跟踪ID获取处理状态。</p>
     *
     * @param trackId 跟踪ID
     * @return 处理状态
     */
    @Get(ApiConst.Endpoints.DOCS_TRACK_STATUS)
    DocumentTrackResponse getTrackStatus(@Var("track_id") String trackId);

    /**
     * 获取文档列表（使用分页请求对象）
     *
     * <p>使用统一的分页请求对象获取文档列表，简化参数传递。</p>
     *
     * @param pageRequest 分页请求对象，包含所有分页和过滤条件
     * @return 分页的文档列表
     */
    @Post(ApiConst.Endpoints.DOCS_PAGINATED)
    DocumentsPaginatedResponse getDocumentsPaginated(@Body PageRequest pageRequest);

    /**
     * 获取文档状态统计
     * 
     * <p>获取文档处理状态的统计信息，包括各状态的数量分布。</p>
     * 
     * @return 文档状态统计信息
     */
    @Get(ApiConst.Endpoints.DOCS_STATUS_COUNTS)
    Map<String, Integer> getDocumentStatusCounts();


}