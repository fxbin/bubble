package cn.fxbin.bubble.ai.lightrag.service;

import cn.fxbin.bubble.ai.lightrag.client.*;
import cn.fxbin.bubble.ai.lightrag.enums.QueryMode;
import cn.fxbin.bubble.ai.lightrag.exception.LightRagServiceException;
import cn.fxbin.bubble.ai.lightrag.model.*;
import cn.fxbin.bubble.ai.lightrag.model.BatchDeleteRequest;
import cn.fxbin.bubble.ai.lightrag.model.LoginRequest;
import cn.fxbin.bubble.ai.lightrag.model.PageRequest;
import cn.fxbin.bubble.ai.lightrag.model.document.*;
import cn.fxbin.bubble.ai.lightrag.model.graph.*;
import cn.fxbin.bubble.ai.lightrag.model.ollama.*;
import cn.fxbin.bubble.ai.lightrag.model.query.QueryRequest;
import com.dtflys.forest.http.ForestSSE;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * LightRAG 核心服务类
 *
 * <p>提供 LightRAG 知识图谱系统的统一服务接口，封装文档管理、知识查询、
 * 图谱管理等核心功能。该服务类作为业务层的主要入口，提供高级抽象和
 * 便捷的操作方法，简化客户端的使用复杂度。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>文档管理：文档插入、更新、删除、查询等操作</li>
 *   <li>知识查询：多模式智能问答和语义检索</li>
 *   <li>图谱管理：知识图谱的构建、优化、导入导出</li>
 *   <li>异步处理：支持异步操作和批量处理</li>
 *   <li>异常处理：统一的异常处理和错误恢复机制</li>
 *   <li>性能监控：操作统计和性能指标收集</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li><strong>统一接口</strong>：提供一致的 API 设计和调用方式</li>
 *   <li><strong>异常安全</strong>：完善的异常处理和错误恢复机制</li>
 *   <li><strong>性能优化</strong>：支持批量操作和异步处理</li>
 *   <li><strong>可观测性</strong>：详细的日志记录和性能监控</li>
 *   <li><strong>扩展性</strong>：灵活的配置和可插拔的组件设计</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private LightRagService lightRagService;
 * 
 * // 插入文档
 * DocumentRequest docRequest = DocumentRequest.builder()
 *     .text("人工智能是计算机科学的一个分支...")
 *     .description("AI基础知识")
 *     .build();
 * DocumentResponse docResponse = lightRagService.insertDocument(docRequest);
 * 
 * // 智能查询
 * QueryRequest queryRequest = QueryRequest.builder()
 *     .query("什么是人工智能？")
 *     .mode(QueryRequest.QueryMode.HYBRID)
 *     .maxResults(5)
 *     .build();
 * QueryResponse queryResponse = lightRagService.query(queryRequest);
 * 
 * // 异步批量处理
 * List<DocumentRequest> documents = Arrays.asList(doc1, doc2, doc3);
 * CompletableFuture<List<DocumentResponse>> future = 
 *     lightRagService.insertDocumentsAsync(documents);
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LightRagService {

    private final LightRagDocumentClient documentClient;
    private final LightRagQueryClient queryClient;
    private final LightRagGraphClient graphClient;
    private final LightRagOllamaClient ollamaClient;
    private final LightRagDefaultClient lightRagDefaultClient;

    // ==================== 文档管理相关方法 ====================

    /**
     * 插入文档到知识库
     *
     * <p>将文档内容插入到 LightRAG 知识库中，系统会自动进行
     * 实体抽取、关系识别和知识图谱构建。</p>
     *
     * @param request 文档插入请求
     * @return 插入结果，包含文档ID和处理状态
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentInsertResponse insertDocument(DocumentRequest request) {
        log.info("开始插入文档，描述: {}", request.getFileSource());

        try {
            validateDocumentRequest(request);

            return documentClient.insertText(request);
        } catch (Exception e) {
            log.error("文档插入过程中发生异常", e);
            throw new LightRagServiceException("文档插入异常", e);
        }
    }

    /**
     * 批量插入文档
     *
     * <p>批量插入多个文档，提高处理效率。支持部分成功模式，
     * 即使某些文档插入失败，其他文档仍可正常处理。</p>
     *
     * @param request 文档插入请求列表
     * @return 插入结果列表
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentInsertResponse insertDocuments(DocumentBatchRequest request) {
        log.info("开始批量插入文档，数量: {}", request.getTexts().size());

        try {
            validateDocumentRequests(request);

            return documentClient.insertTexts(request);
        } catch (Exception e) {
            log.error("批量文档插入过程中发生异常", e);
            throw new LightRagServiceException("批量文档插入异常", e);
        }
    }

    /**
     * 异步插入文档
     *
     * <p>异步方式插入文档，不阻塞当前线程，适用于大量文档
     * 的批量处理场景。</p>
     *
     * @param request 文档插入请求
     * @return 异步插入结果
     */
    public CompletableFuture<DocumentInsertResponse> insertDocumentAsync(DocumentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return insertDocument(request);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * 异步批量插入文档
     *
     * <p>异步方式批量插入文档，提供更好的并发处理能力。</p>
     *
     * @param request 文档插入请求列表
     * @return 异步批量插入结果
     */
    public CompletableFuture<DocumentInsertResponse> insertDocumentsAsync(DocumentBatchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return insertDocuments(request);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * 删除文档
     *
     * <p>从知识库中删除指定文档，同时清理相关的知识图谱数据。</p>
     *
     * @param documentId 文档ID
     * @return 删除结果
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentDeleteResponse deleteDocument(String documentId) {
        log.info("开始删除文档，文档ID: {}", documentId);

        try {
            validateDocumentId(documentId);

            BatchDeleteRequest deleteRequest = BatchDeleteRequest.builder()
                    .docIds(Lists.newArrayList(documentId))
                    .build();
            return documentClient.deleteDocuments(deleteRequest);
        } catch (Exception e) {
            log.error("文档删除过程中发生异常，文档ID: {}", documentId, e);
            throw new LightRagServiceException("文档删除异常", e);
        }
    }

    /**
     * 扫描新文档
     *
     * <p>触发扫描输入目录中的新文档，并启动处理流程。
     * 如果扫描流程已在运行，则返回相应状态。</p>
     *
     * @return 扫描状态，包含跟踪ID和处理状态
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentScanResponse scanDocuments() {
        log.info("开始扫描新文档");

        try {
            return documentClient.scanDocuments();
        } catch (Exception e) {
            log.error("文档扫描过程中发生异常", e);
            throw new LightRagServiceException("文档扫描异常", e);
        }
    }

    /**
     * 上传文件到输入目录
     *
     * <p>上传文件到输入目录并进行索引处理。
     * 支持多种文件格式，如PDF、Word、文本文件等。</p>
     *
     * @param file 文件路径
     * @return 上传结果，包含状态和处理信息
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentInsertResponse uploadDocument(File file) {
        log.info("开始上传文档，文件路径: {}", file);

        try {
            validateFilePath(file);
            return documentClient.uploadDocument(file);
        } catch (Exception e) {
            log.error("文档上传过程中发生异常，文件路径: {}", file, e);
            throw new LightRagServiceException("文档上传异常", e);
        }
    }

    /**
     * 查询文档跟踪状态
     *
     * <p>根据文档处理的跟踪ID查询文档的处理状态。</p>
     *
     * @param trackId 文档处理跟踪ID
     * @return 文档跟踪状态
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentTrackResponse getDocumentTrackStatus(String trackId) {
        log.info("开始查询文档跟踪，跟踪ID: {}", trackId);

        try {
            return documentClient.getTrackStatus(trackId);
        } catch (Exception e) {
            log.error("文档跟踪查询过程中发生异常，跟踪ID: {}", trackId, e);
            throw new LightRagServiceException("文档跟踪查询异常", e);
        }
    }

    /**
     * 获取文档列表
     *
     * <p>分页获取文档列表，支持按多种条件进行过滤和排序。</p>
     *
     * @return 分页的文档列表
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentStatusResponse getDocuments() {
        log.debug("查询文档列表");

        try {
            return documentClient.getDocuments();
        } catch (Exception e) {
            log.error("文档列表查询过程中发生异常", e);
            throw new LightRagServiceException("文档列表查询异常", e);
        }
    }


    /**
     * 获取文档列表（使用分页请求对象）
     *
     * <p>使用统一的分页请求对象获取文档列表，简化参数传递。</p>
     *
     * @param pageRequest 分页请求对象，包含所有分页和过滤条件
     * @return 分页的文档列表
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentsPaginatedResponse getDocumentsPaginated(PageRequest pageRequest) {
        log.debug("查询文档列表，分页请求: {}", pageRequest);

        try {
            validatePageRequest(pageRequest);
            return documentClient.getDocumentsPaginated(pageRequest);
        } catch (Exception e) {
            log.error("分页文档列表查询过程中发生异常", e);
            throw new LightRagServiceException("分页文档列表查询异常", e);
        }
    }

    /**
     * 获取文档状态统计
     *
     * <p>获取文档处理状态的统计信息，包括各状态的数量分布。</p>
     *
     * @return 文档状态统计信息
     * @throws LightRagServiceException 服务处理异常
     */
    public Map<String, Integer> getDocumentStatusCounts() {
        log.debug("查询文档状态统计");

        try {
            return documentClient.getDocumentStatusCounts();
        } catch (Exception e) {
            log.error("文档状态统计查询过程中发生异常", e);
            throw new LightRagServiceException("文档状态统计查询异常", e);
        }
    }

    /**
     * 获取处理管道状态
     *
     * <p>获取处理管道状态信息，了解当前系统的处理能力和状态。</p>
     *
     * @return 处理管道状态
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentPipelineStatusResponse getPipelineStatus() {
        log.debug("查询处理管道状态");

        try {
            return documentClient.getPipelineStatus();
        } catch (Exception e) {
            log.error("处理管道状态查询过程中发生异常", e);
            throw new LightRagServiceException("处理管道状态查询异常", e);
        }
    }

    /**
     * 获取跟踪状态
     *
     * <p>根据跟踪ID获取处理状态，用于追踪异步操作的进度。</p>
     *
     * @param trackId 跟踪ID
     * @return 处理状态
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentTrackResponse getTrackStatus(String trackId) {
        log.debug("查询跟踪状态，跟踪ID: {}", trackId);

        try {
            validateTrackId(trackId);
            return documentClient.getTrackStatus(trackId);
        } catch (Exception e) {
            log.error("跟踪状态查询过程中发生异常，跟踪ID: {}", trackId, e);
            throw new LightRagServiceException("跟踪状态查询异常", e);
        }
    }



    // ==================== 知识查询相关方法 ====================

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
     * @param request 查询请求，包含查询文本、查询模式、过滤条件等
     * @return 查询结果，包含相关实体、关系、摘要信息等
     * @throws LightRagServiceException 服务处理异常
     */
    public QueryResponse query(QueryRequest request) {
        log.info("开始知识查询，查询内容: {}, 模式: {}", request.getQuery(), request.getMode());
        
        try {
            validateQueryRequest(request);
            
            return queryClient.query(request);
        } catch (Exception e) {
            log.error("知识查询过程中发生异常，查询内容: {}", request.getQuery(), e);
            throw new LightRagServiceException("知识查询异常", e);
        }
    }

    /**
     * 简单查询
     *
     * <p>提供简化的查询接口，使用默认的混合查询模式。</p>
     *
     * @param query 查询文本
     * @return 查询结果
     * @throws LightRagServiceException 服务处理异常
     */
    public QueryResponse simpleQuery(String query) {
        log.info("开始简单查询，查询内容: {}", query);
        
        try {
            validateQueryText(query);
            
            QueryRequest request = QueryRequest.builder()
                    .query(query)
                    .mode(QueryMode.HYBRID)
                    .build();
            
            return queryClient.query(request);
        } catch (Exception e) {
            log.error("简单查询过程中发生异常，查询内容: {}", query, e);
            throw new LightRagServiceException("简单查询异常", e);
        }
    }

    /**
     * 批量查询
     *
     * <p>批量执行多个查询请求，提高处理效率。</p>
     *
     * @param requests 查询请求列表
     * @return 查询结果列表
     * @throws LightRagServiceException 服务处理异常
     */
    public List<QueryResponse> batchQuery(List<QueryRequest> requests) {
        log.info("开始批量查询，查询数量: {}", requests.size());
        
        try {
            validateQueryRequests(requests);
            
            List<QueryResponse> responses = new ArrayList<>();
            for (QueryRequest request : requests) {
                try {
                    QueryResponse response = queryClient.query(request);
                    responses.add(response);
                } catch (Exception e) {
                    log.warn("批量查询中单个查询失败，查询内容: {}", request.getQuery(), e);
                    // 为失败的查询创建错误响应
                    QueryResponse errorResponse = QueryResponse.builder()
                            .response("查询失败: " + e.getMessage())
                            .build();
                    responses.add(errorResponse);
                }
            }
            
            return responses;
        } catch (Exception e) {
            log.error("批量查询过程中发生异常", e);
            throw new LightRagServiceException("批量查询异常", e);
        }
    }

    /**
     * 异步查询
     *
     * <p>异步方式执行查询，不阻塞当前线程。</p>
     *
     * @param request 查询请求
     * @return 异步查询结果
     */
    public CompletableFuture<QueryResponse> queryAsync(QueryRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return query(request);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * 简单流式查询
     *
     * <p>提供简化的流式查询接口，使用默认的混合查询模式。</p>
     *
     * @param query 查询文本
     * @return 流式查询结果的 Flux 流
     * @throws LightRagServiceException 服务处理异常
     */
    public Flux<String> simpleStreamQuery(String query) {
        log.info("开始简单流式查询，查询内容: {}", query);
        
        try {
            validateQueryText(query);
            
            QueryRequest request = QueryRequest.builder()
                    .query(query)
                    .mode(QueryMode.HYBRID)
                    .build();
            
            return streamQuery(request);
        } catch (Exception e) {
            log.error("简单流式查询过程中发生异常，查询内容: {}", query, e);
            return Flux.error(new LightRagServiceException("简单流式查询异常", e));
        }
    }

    /**
     * 流式查询（SSE）
     *
     * <p>通过 Server-Sent Events (SSE) 执行流式查询，实时返回查询结果。
     * 使用 WebFlux 的 Flux 流式处理，适用于长时间查询或需要实时反馈的场景。</p>
     *
     * <h4>事件类型：</h4>
     * <ul>
     *   <li><strong>progress</strong>：查询进度更新</li>
     *   <li><strong>result</strong>：部分查询结果</li>
     *   <li><strong>error</strong>：查询过程中的错误</li>
     *   <li><strong>complete</strong>：查询完成</li>
     * </ul>
     *
     * <h4>使用示例：</h4>
     * <pre>{@code
     * QueryRequest request = QueryRequest.builder()
     *     .query("人工智能在医疗领域的应用")
     *     .mode(QueryRequest.QueryMode.HYBRID)
     *     .build();
     *
     * Flux<String> stream = lightRagService.streamQuery(request);
     * stream.subscribe(
     *     data -> System.out.println("接收到数据: " + data),
     *     error -> System.err.println("发生错误: " + error),
     *     () -> System.out.println("查询完成")
     * );
     * }</pre>
     *
     * @param request 查询请求
     * @return 流式查询结果的 Flux 流
     * @throws LightRagServiceException 服务处理异常
     */
    public Flux<String> streamQuery(QueryRequest request) {
        log.info("开始流式查询，查询内容: {}, 模式: {}", request.getQuery(), request.getMode());
        
        try {
            validateQueryRequest(request);
            
            // 创建 Sinks 用于发送数据
            Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
            
            // 异步执行 SSE 查询
            CompletableFuture.runAsync(() -> {
                try {
                    ForestSSE sse = queryClient.streamQuery(request);
                    
                    sse.setOnOpen(event -> {
                        log.debug("SSE 连接已建立");
                        sink.tryEmitNext("{\"event\":\"open\",\"message\":\"连接已建立\"}");
                    })
                    .setOnMessage(event -> {
                        String eventType = event.event();
                        String data = event.data();
                        String eventId = event.id();
                        
                        log.debug("接收到 SSE 事件 - 类型: {}, ID: {}, 数据: {}", eventType, eventId, data);
                        
                        // 构造标准化的事件数据
                        String eventData = String.format(
                            "{\"event\":\"%s\",\"id\":\"%s\",\"data\":%s}",
                            eventType != null ? eventType : "message",
                            eventId != null ? eventId : "",
                            data != null ? data : "\"\""
                        );
                        
                        sink.tryEmitNext(eventData);
                    })
                    .setOnClose(event -> {
                        log.debug("SSE 连接已关闭");
                        sink.tryEmitNext("{\"event\":\"complete\",\"message\":\"查询完成\"}");
                        sink.tryEmitComplete();
                    })
                    .listen(); // 开始监听 SSE 事件
                    
                } catch (Exception e) {
                    log.error("流式查询过程中发生异常", e);
                    sink.tryEmitNext(String.format(
                        "{\"event\":\"error\",\"message\":\"%s\"}",
                        e.getMessage()
                    ));
                    sink.tryEmitError(new LightRagServiceException("流式查询异常", e));
                }
            });
            
            return sink.asFlux()
                    // 设置超时时间
                .timeout(Duration.ofMinutes(5))
                .doOnSubscribe(subscription -> log.debug("开始订阅流式查询结果"))
                .doOnNext(data -> log.debug("发送流式数据: {}", data))
                .doOnComplete(() -> log.info("流式查询完成"))
                .doOnError(error -> log.error("流式查询发生错误", error))
                .doOnCancel(() -> log.debug("流式查询被取消"));
                
        } catch (Exception e) {
            log.error("启动流式查询过程中发生异常", e);
            return Flux.error(new LightRagServiceException("启动流式查询异常", e));
        }
    }



    /**
     * 查询建议
     *
     * <p>基于用户输入的部分查询内容，提供查询建议和自动补全功能。</p>
     *
     * @param partialQuery 部分查询内容
     * @param maxSuggestions 最大建议数量
     * @return 查询建议列表
     * @throws LightRagServiceException 服务处理异常
     */
    public List<String> getQuerySuggestions(String partialQuery, Integer maxSuggestions) {
        log.info("获取查询建议，部分查询: {}, 最大建议数: {}", partialQuery, maxSuggestions);
        
        try {
            validatePartialQuery(partialQuery);
            if (maxSuggestions == null || maxSuggestions <= 0) {
                maxSuggestions = 5;
            }
            
            // 这里可以实现基于历史查询、知识图谱实体等的建议逻辑
            // 目前返回基础建议
            List<String> suggestions = new ArrayList<>();
            suggestions.add(partialQuery + "的定义是什么？");
            suggestions.add(partialQuery + "有哪些应用场景？");
            suggestions.add(partialQuery + "的发展历史如何？");
            suggestions.add(partialQuery + "与其他概念的关系是什么？");
            suggestions.add(partialQuery + "的优缺点有哪些？");
            
            return suggestions.stream()
                    .limit(maxSuggestions)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取查询建议过程中发生异常，部分查询: {}", partialQuery, e);
            throw new LightRagServiceException("获取查询建议异常", e);
        }
    }

    /**
     * 相似查询推荐
     *
     * <p>基于当前查询内容，推荐相似的查询问题。</p>
     *
     * @param query 当前查询内容
     * @param maxRecommendations 最大推荐数量
     * @return 相似查询推荐列表
     * @throws LightRagServiceException 服务处理异常
     */
    public List<String> getSimilarQueries(String query, Integer maxRecommendations) {
        log.info("获取相似查询推荐，查询: {}, 最大推荐数: {}", query, maxRecommendations);
        
        try {
            validateQueryText(query);
            if (maxRecommendations == null || maxRecommendations <= 0) {
                maxRecommendations = 5;
            }
            
            // 这里可以实现基于语义相似度的推荐逻辑
            // 目前返回基础推荐
            List<String> recommendations = new ArrayList<>();
            recommendations.add("与" + query + "相关的概念有哪些？");
            recommendations.add(query + "的实际应用案例");
            recommendations.add(query + "的技术原理解析");
            recommendations.add(query + "的发展趋势预测");
            recommendations.add(query + "与传统方法的对比");
            
            return recommendations.stream()
                    .limit(maxRecommendations)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取相似查询推荐过程中发生异常，查询: {}", query, e);
            throw new LightRagServiceException("获取相似查询推荐异常", e);
        }
    }

    // ==================== 图谱管理相关方法 ====================

    /**
     * 获取知识图谱
     *
     * <p>获取指定标签的知识图谱子图。</p>
     *
     * @param label 标签名称
     * @param maxDepth 最大深度
     * @param maxNodes 最大节点数
     * @return 知识图谱数据
     * @throws LightRagServiceException 服务处理异常
     */
    public GraphResponse getKnowledgeGraph(String label, Integer maxDepth, Integer maxNodes) {
        log.info("获取知识图谱，标签: {}, 最大深度: {}, 最大节点数: {}", label, maxDepth, maxNodes);
        
        try {
            validateGraphQueryParameters(label, maxDepth, maxNodes);
            
            return graphClient.getKnowledgeGraph(label, maxDepth, maxNodes);
        } catch (Exception e) {
            log.error("获取知识图谱过程中发生异常，标签: {}", label, e);
            throw new LightRagServiceException("获取知识图谱异常", e);
        }
    }

    /**
     * 获取图标签列表
     *
     * <p>获取所有可用的图标签。</p>
     *
     * @return 标签列表
     * @throws LightRagServiceException 服务处理异常
     */
    public List<String> getGraphLabels() {
        log.debug("获取图标签列表");
        
        try {
            return graphClient.getGraphLabels();
        } catch (Exception e) {
            log.error("获取图标签列表过程中发生异常", e);
            throw new LightRagServiceException("获取图标签列表异常", e);
        }
    }

    /**
     * 检查实体是否存在
     *
     * <p>检查指定名称的实体是否存在。</p>
     *
     * @param entityName 实体名称
     * @return 存在性检查结果
     * @throws LightRagServiceException 服务处理异常
     */
    public GraphExistsResponse checkEntityExists(String entityName) {
        log.debug("检查实体是否存在，实体名称: {}", entityName);
        
        try {
            validateEntityName(entityName);
            
            return graphClient.checkEntityExists(entityName);
        } catch (Exception e) {
            log.error("检查实体存在性过程中发生异常，实体名称: {}", entityName, e);
            throw new LightRagServiceException("检查实体存在性异常", e);
        }
    }

    /**
     * 更新实体
     *
     * <p>更新实体的属性信息。</p>
     *
     * @param entityName 实体名称
     * @param updatedData 更新的数据
     * @return 更新结果
     * @throws LightRagServiceException 服务处理异常
     */
    public Map<String, Object> updateEntity(String entityName, Map<String, Object> updatedData) {
        log.info("更新实体，实体名称: {}", entityName);
        
        try {
            validateEntityUpdateRequest(entityName, updatedData);
            
            GraphEditRequest request = GraphEditRequest.builder()
                    .entityName(entityName)
                    .updatedData(updatedData)
                    .build();
            
            return graphClient.updateEntity(request);
        } catch (Exception e) {
            log.error("更新实体过程中发生异常，实体名称: {}", entityName, e);
            throw new LightRagServiceException("更新实体异常", e);
        }
    }

    /**
     * 更新关系
     *
     * <p>更新关系的属性信息。</p>
     *
     * @param sourceEntity 源实体
     * @param targetEntity 目标实体
     * @param relationshipType 关系类型
     * @param updatedData 更新的数据
     * @return 更新结果
     * @throws LightRagServiceException 服务处理异常
     */
    public Map<String, Object> updateRelation(String sourceEntity, String targetEntity, 
                                            String relationshipType, Map<String, Object> updatedData) {
        log.info("更新关系，源实体: {}, 目标实体: {}, 关系类型: {}", sourceEntity, targetEntity, relationshipType);
        
        try {
            validateRelationUpdateRequest(sourceEntity, targetEntity, relationshipType, updatedData);
            
            GraphRelationEditRequest request = GraphRelationEditRequest.builder()
                    .sourceId(sourceEntity)
                    .targetId(targetEntity)
                    .updatedData(updatedData)
                    .build();
            
            return graphClient.updateRelation(request);
        } catch (Exception e) {
            log.error("更新关系过程中发生异常，源实体: {}, 目标实体: {}, 关系类型: {}", sourceEntity, targetEntity, relationshipType, e);
            throw new LightRagServiceException("更新关系异常", e);
        }
    }

    // ==================== Ollama兼容接口方法 ====================

    /**
     * 获取版本信息
     *
     * <p>获取 Ollama 兼容的版本信息。</p>
     *
     * @return 版本信息
     * @throws LightRagServiceException 服务处理异常
     */
    public VersionInfo getVersion() {
        log.debug("获取版本信息");
        
        try {
            return ollamaClient.getVersion();
        } catch (Exception e) {
            log.error("获取版本信息过程中发生异常", e);
            throw new LightRagServiceException("获取版本信息异常", e);
        }
    }

    /**
     * 获取可用模型列表
     *
     * <p>返回作为 Ollama 服务器可用的模型列表。</p>
     *
     * @return 模型列表
     * @throws LightRagServiceException 服务处理异常
     */
    public ModelInfo getAvailableModels() {
        log.debug("获取可用模型列表");
        
        try {
            return ollamaClient.getTags();
        } catch (Exception e) {
            log.error("获取可用模型列表过程中发生异常", e);
            throw new LightRagServiceException("获取可用模型列表异常", e);
        }
    }

    /**
     * 获取运行中的模型
     *
     * <p>列出当前运行中的模型。</p>
     *
     * @return 运行中的模型列表
     * @throws LightRagServiceException 服务处理异常
     */
    public ModelInfo getRunningModels() {
        log.debug("获取运行中的模型");
        
        try {
            return ollamaClient.getRunningModels();
        } catch (Exception e) {
            log.error("获取运行中的模型过程中发生异常", e);
            throw new LightRagServiceException("获取运行中的模型异常", e);
        }
    }

    /**
     * 文本生成
     *
     * <p>处理生成完成请求，作为 Ollama 模型处理。</p>
     *
     * @param request 生成请求
     * @return 生成结果
     * @throws LightRagServiceException 服务处理异常
     */
    public OllamaGenerateResponse generate(OllamaGenerateRequest request) {
        log.info("执行文本生成");
        
        try {
            if (request == null) {
                throw new IllegalArgumentException("生成请求不能为空");
            }
            
            return ollamaClient.generate(request);
        } catch (Exception e) {
            log.error("文本生成过程中发生异常", e);
            throw new LightRagServiceException("文本生成异常", e);
        }
    }

    /**
     * 对话聊天
     *
     * <p>处理聊天完成请求，作为 Ollama 模型处理。</p>
     *
     * @param request 聊天请求
     * @return 聊天结果
     * @throws LightRagServiceException 服务处理异常
     */
    public OllamaChatResponse chat(OllamaChatRequest request) {
        log.info("执行对话聊天");
        
        try {
            if (request == null) {
                throw new IllegalArgumentException("聊天请求不能为空");
            }
            
            return ollamaClient.chat(request);
        } catch (Exception e) {
            log.error("对话聊天过程中发生异常", e);
            throw new LightRagServiceException("对话聊天异常", e);
        }
    }

    // ==================== 系统管理相关方法 ====================

    /**
     * 健康检查
     *
     * <p>检查查询服务的健康状态，包括服务可用性、
     * 响应时间、处理能力等。</p>
     *
     * @return 健康检查结果
     * @throws LightRagServiceException 服务处理异常
     */
    public HealthCheckResponse checkHealth() {
        log.debug("执行健康检查");
        
        try {
            return lightRagDefaultClient.healthCheck();
        } catch (Exception e) {
            log.error("健康检查过程中发生异常", e);
            throw new LightRagServiceException("健康检查异常", e);
        }
    }

    /**
     * 获取认证状态
     *
     * <p>获取当前系统的认证状态信息，包括是否启用认证、
     * 当前认证状态以及访客令牌等信息。</p>
     *
     * @return 认证状态信息
     * @throws LightRagServiceException 服务处理异常
     */
    public AuthStatusResponse getAuthStatus() {
        log.debug("获取认证状态");
        
        try {
            return lightRagDefaultClient.getAuthStatus();
        } catch (Exception e) {
            log.error("获取认证状态过程中发生异常", e);
            throw new LightRagServiceException("获取认证状态异常", e);
        }
    }

    /**
     * 用户登录
     *
     * <p>使用用户名和密码进行身份认证，成功后返回访问令牌
     * 和相关的认证信息。</p>
     *
     * @param loginRequest 登录请求参数，包含用户名、密码等信息
     * @return 登录结果，包含访问令牌等认证信息
     * @throws LightRagServiceException 服务处理异常
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录，用户名: {}", loginRequest.getUsername());
        
        try {
            validateLoginRequest(loginRequest);
            
            return lightRagDefaultClient.login(loginRequest);
        } catch (Exception e) {
            log.error("用户登录过程中发生异常，用户名: {}", loginRequest.getUsername(), e);
            throw new LightRagServiceException("用户登录异常", e);
        }
    }

    /**
     * 批量删除文档
     *
     * <p>批量删除指定的文档列表，提高删除效率。</p>
     *
     * @param documentIds 文档ID列表
     * @return 删除结果
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentDeleteResponse batchDeleteDocuments(List<String> documentIds) {
        log.info("批量删除文档，数量: {}", documentIds.size());
        
        try {
            if (documentIds == null || documentIds.isEmpty()) {
                throw new IllegalArgumentException("文档ID列表不能为空");
            }
            
            for (String docId : documentIds) {
                validateDocumentId(docId);
            }
            
            BatchDeleteRequest deleteRequest = BatchDeleteRequest.builder()
                    .docIds(documentIds)
                    .build();
            
            return documentClient.deleteDocuments(deleteRequest);
        } catch (Exception e) {
            log.error("批量删除文档过程中发生异常", e);
            throw new LightRagServiceException("批量删除文档异常", e);
        }
    }

    /**
     * 清除文档缓存
     *
     * <p>清除指定类型的文档缓存，释放内存资源。</p>
     *
     * @param cacheType 缓存类型
     * @return 清除结果
     * @throws LightRagServiceException 服务处理异常
     */
    public DocumentResponse clearDocumentCache(String cacheType) {
        log.info("清除文档缓存，缓存类型: {}", cacheType);
        
        try {
            if (!StringUtils.hasText(cacheType)) {
                throw new IllegalArgumentException("缓存类型不能为空");
            }
            
            DocumentClearCacheRequest request = DocumentClearCacheRequest.builder()
                    .modes(Lists.newArrayList(cacheType))
                    .build();
            
            return documentClient.clearCache(request);
        } catch (Exception e) {
            log.error("清除文档缓存过程中发生异常，缓存类型: {}", cacheType, e);
            throw new LightRagServiceException("清除文档缓存异常", e);
        }
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 验证文档请求参数
     */
    private void validateDocumentRequest(DocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("文档请求不能为空");
        }
        if (!StringUtils.hasText(request.getText())) {
            throw new IllegalArgumentException("文档内容不能为空");
        }
    }

    /**
     * 验证批量文档请求参数
     */
    private void validateDocumentRequests(DocumentBatchRequest request) {
        if (request == null || request.getTexts() == null || request.getTexts().isEmpty()) {
            throw new IllegalArgumentException("文档请求列表不能为空");
        }
        request.getTexts().forEach(text -> {
            if (!StringUtils.hasText(text)) {
                throw new IllegalArgumentException("文档内容不能为空");
            }
        });
    }

    /**
     * 验证文件路径
     */
    private void validateFilePath(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件路径无效或文件不存在");
        }
    }

    /**
     * 验证分页请求参数
     */
    private void validatePageRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            throw new IllegalArgumentException("分页请求不能为空");
        }
        if (pageRequest.getPage() != null && pageRequest.getPage() < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageRequest.getPageSize() != null && (pageRequest.getPageSize() < 1 || pageRequest.getPageSize() > 100)) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }

    /**
     * 验证跟踪ID
     */
    private void validateTrackId(String trackId) {
        if (!StringUtils.hasText(trackId)) {
            throw new IllegalArgumentException("跟踪ID不能为空");
        }
    }



    /**
     * 验证实体名称
     */
    private void validateEntityName(String entityName) {
        if (!StringUtils.hasText(entityName)) {
            throw new IllegalArgumentException("实体名称不能为空");
        }
    }

    /**
     * 验证关系参数
     */
    private void validateRelationParameters(String sourceEntity, String targetEntity, String relationshipType) {
        if (!StringUtils.hasText(sourceEntity)) {
            throw new IllegalArgumentException("源实体不能为空");
        }
        if (!StringUtils.hasText(targetEntity)) {
            throw new IllegalArgumentException("目标实体不能为空");
        }
        if (!StringUtils.hasText(relationshipType)) {
            throw new IllegalArgumentException("关系类型不能为空");
        }
    }

    /**
     * 验证文档ID
     */
    private void validateDocumentId(String documentId) {
        if (!StringUtils.hasText(documentId)) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
    }

    /**
     * 验证查询请求参数
     */
    private void validateQueryRequest(QueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("查询请求不能为空");
        }
        if (!StringUtils.hasText(request.getQuery())) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
    }

    /**
     * 验证批量查询请求参数
     */
    private void validateQueryRequests(List<QueryRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("查询请求列表不能为空");
        }
        requests.forEach(this::validateQueryRequest);
    }

    /**
     * 验证部分查询内容
     */
    private void validatePartialQuery(String partialQuery) {
        if (!StringUtils.hasText(partialQuery)) {
            throw new IllegalArgumentException("部分查询内容不能为空");
        }
        if (partialQuery.trim().length() < 1) {
            throw new IllegalArgumentException("部分查询内容长度不能小于1个字符");
        }
    }

    /**
     * 验证查询文本
     */
    private void validateQueryText(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("查询文本不能为空");
        }
        if (query.trim().length() < 1) {
            throw new IllegalArgumentException("查询文本长度不能小于1个字符");
        }
    }

    /**
     * 验证图谱查询参数
     */
    private void validateGraphQueryParameters(String label, Integer maxDepth, Integer maxNodes) {
        if (maxDepth != null && maxDepth < 1) {
            throw new IllegalArgumentException("最大深度必须大于0");
        }
        if (maxNodes != null && maxNodes < 1) {
            throw new IllegalArgumentException("最大节点数必须大于0");
        }
    }

    /**
     * 验证实体更新请求参数
     */
    private void validateEntityUpdateRequest(String entityName, Map<String, Object> updatedData) {
        validateEntityName(entityName);
        if (updatedData == null || updatedData.isEmpty()) {
            throw new IllegalArgumentException("更新数据不能为空");
        }
    }

    /**
     * 验证关系更新请求参数
     */
    private void validateRelationUpdateRequest(String sourceEntity, String targetEntity, 
                                             String relationshipType, Map<String, Object> updatedData) {
        validateRelationParameters(sourceEntity, targetEntity, relationshipType);
        if (updatedData == null || updatedData.isEmpty()) {
            throw new IllegalArgumentException("更新数据不能为空");
        }
    }

    /**
     * 验证登录请求参数
     */
    private void validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest == null) {
            throw new IllegalArgumentException("登录请求不能为空");
        }
        if (!StringUtils.hasText(loginRequest.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(loginRequest.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}

