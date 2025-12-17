package cn.fxbin.bubble.flow.core.context;

import cn.hutool.core.date.SystemClock;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.flow.core.cache.FlowNodeCache;
import cn.fxbin.bubble.flow.core.enums.PluginType;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.fxbin.bubble.flow.core.state.cache.CaffeineFlowStateCache;
import cn.fxbin.bubble.flow.core.state.cache.FlowStateCache;
import cn.fxbin.bubble.flow.core.state.cache.RedisFlowStateCache;
import cn.fxbin.bubble.flow.core.state.serializer.FlowStateSerializer;
import cn.fxbin.bubble.flow.core.state.serializer.JsonFlowStateSerializer;
import cn.fxbin.bubble.flow.core.state.serializer.SerializationException;
import cn.fxbin.bubble.flow.core.util.FlowUtils;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FlowContextHolder
 * 工作流上下文持有者，负责管理工作流执行过程中的状态
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:14
 */
@Slf4j
@RequiredArgsConstructor
public class FlowContextHolder implements Serializable {


    private static final int VERSION = 1;
    private static final long DEFAULT_CACHE_EXPIRE_HOURS = 1;
    private static final long DEFAULT_MAX_CACHE_SIZE = 1000;
    private static final long DEFAULT_REDIS_EXPIRE_HOURS = 2;

    // L1 Cache (Caffeine)
    private static final FlowStateCache L1_CACHE;
    // L2 Cache (Redis)
    private static final FlowStateCache L2_CACHE;
    // Serializer
    private static final FlowStateSerializer STATE_SERIALIZER;

    static {
        L1_CACHE = new CaffeineFlowStateCache(DEFAULT_CACHE_EXPIRE_HOURS, DEFAULT_MAX_CACHE_SIZE);
        try {
            L2_CACHE = new RedisFlowStateCache(DEFAULT_REDIS_EXPIRE_HOURS);
        } catch (IllegalStateException e) {
            log.warn("Failed to initialize RedisFlowStateCache. L2 cache will be unavailable.", e);
            // L2_CACHE = new NoOpFlowStateCache(); // Or some other fallback if needed
            // Or handle as appropriate for your application
            throw e;
        }
        STATE_SERIALIZER = new JsonFlowStateSerializer();
    }

    /**
     * 流程ID
     */
    @Getter
    private final Long flowId;


    /**
     * 流程开始时间
     */
    @Getter
    private final long startTime;

    /**
     * 流程执行ID
     */
    @Getter
    private final String executionId;

    /**
     * 流程版本号
     */
    @Setter
    @Getter
    private Integer flowVersion;

    /**
     * 用户ID
     */
    @Setter
    @Getter
    private String userId;

    /**
     * 租户ID
     */
    @Setter
    @Getter
    private String tenantId;

    /**
     * 变量存储
     */
    private final Map<String, Object> variables;

    /**
     * 版本执行上下文
     */
    private VersionExecutionContext versionContext;


    /**
     * 创建新的上下文
     *
     * @param flowId 流程ID
     * @return 上下文实例
     */
    public static FlowContextHolder create(Long flowId) {
        return new FlowContextHolder(
                flowId,
                SystemClock.now(),
                FlowUtils.generateExecutionId(flowId),
                Maps.newConcurrentMap()
        );
    }

    /**
     * 创建历史版本执行上下文
     *
     * @param flowId 流程ID
     * @param version 目标版本号
     * @return 上下文实例
     */
    public static FlowContextHolder createForHistoricalVersion(Long flowId, Integer version) {
        FlowContextHolder context = create(flowId);
        VersionExecutionContext versionContext = new VersionExecutionContext()
                .setTargetVersion(version)
                .setHistoricalExecution(true);
        context.setVersionContext(versionContext);
        return context;
    }

    /**
     * 设置变量
     *
     * @param key 变量名
     * @param value 变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 获取变量
     *
     * @param key 变量名
     * @param type 变量类型
     * @param <T> 类型参数
     * @return 变量值
     */
    public <T> T getVariable(String key, Class<T> type) {
        Object value = variables.get(key);
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            log.warn("Variable [{}] is not of type {}", key, type.getName());
            return null;
        }
    }

    /**
     * 获取流程节点
     *
     * @param flowId 流程ID
     * @param nodeId 节点ID
     * @return 流程节点
     */
    public FlowNode getNode(Long flowId, String nodeId) {
        return FlowNodeCache.getNode(flowId, nodeId);
    }

    /**
     * 根据节点类型获取流程节点列表
     *
     * @param flowId 流程ID
     * @param pluginType 节点类型
     * @return 符合指定类型的流程节点列表
     * @author fxbin
     */
    public List<FlowNode> getNodesByType(Long flowId, PluginType pluginType) {
        Collection<FlowNode> allNodes = FlowNodeCache.getAllNodes(flowId);
        if (allNodes == null || allNodes.isEmpty()) {
            return Collections.emptyList();
        }
        return allNodes.stream()
                .filter(node -> node != null && node.getNodeType() == pluginType)
                .collect(Collectors.toList());
    }

    /**
     * 检查变量是否存在
     *
     * @param key 变量名
     * @return 是否存在
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }

    /**
     * 获取所有变量
     *
     * @return 变量映射
     */
    public Map<String, Object> getAllVariables() {
        return variables;
    }

    /**
     * 设置版本执行上下文
     *
     * @param versionContext 版本执行上下文
     */
    public void setVersionContext(VersionExecutionContext versionContext) {
        this.versionContext = versionContext;
    }

    /**
     * 获取版本执行上下文
     *
     * @return 版本执行上下文
     */
    public VersionExecutionContext getVersionContext() {
        return versionContext;
    }

    /**
     * 是否为历史版本执行模式
     *
     * @return 是否为历史版本执行
     */
    public boolean isHistoricalExecution() {
        return versionContext != null && Boolean.TRUE.equals(versionContext.getHistoricalExecution());
    }

    /**
     * 获取目标版本号
     *
     * @return 目标版本号
     */
    public Integer getTargetVersion() {
        return versionContext != null ? versionContext.getTargetVersion() : null;
    }

    /**
     * 获取指定节点的前置节点，不进行类型筛选。
     *
     * @param flowId          流程ID
     * @param currentNodeId   当前节点ID
     * @param recursive       是否递归查找所有前置节点 (true: 是, false: 仅直接前置节点)
     * @return 前置节点列表
     * @author fxbin
     */
    public List<FlowNode> getPreviousNodes(Long flowId, String currentNodeId, boolean recursive) {
        return getPreviousNodes(flowId, currentNodeId, recursive, null);
    }

    /**
     * 获取指定节点的前置节点，并可以根据指定的插件类型进行筛选。
     *
     * @param flowId          流程ID
     * @param currentNodeId   当前节点ID
     * @param recursive       是否递归查找所有前置节点 (true: 是, false: 仅直接前置节点)
     * @param filterType      要筛选的插件类型，如果为 null，则不进行类型筛选
     * @return 符合条件的前置节点列表
     * @author fxbin
     */
    public List<FlowNode> getPreviousNodes(Long flowId, String currentNodeId, boolean recursive, PluginType filterType) {
        List<FlowEdge> allEdges = FlowNodeCache.getAllEdges(flowId);
        if (allEdges == null || allEdges.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> visitedNodeIds = new HashSet<>();
        List<FlowNode> previousNodes = new ArrayList<>();

        if (recursive) {
            findPreviousNodesRecursive(flowId, currentNodeId, allEdges, previousNodes, visitedNodeIds, filterType);
        } else {
            for (FlowEdge edge : allEdges) {
                if (edge.getTargetNodeId().equals(currentNodeId)) {
                    FlowNode prevNode = FlowNodeCache.getNode(flowId, edge.getSourceNodeId());
                    if (prevNode != null && visitedNodeIds.add(prevNode.getId())) {
                        if (filterType == null || prevNode.getNodeType() == filterType) {
                            previousNodes.add(prevNode);
                        }
                    }
                }
            }
        }
        return previousNodes;
    }

    /**
     * 递归查找前置节点。
     *
     * @param flowId          流程ID
     * @param currentNodeId   当前节点ID
     * @param allEdges        流程的所有边
     * @param previousNodes   用于收集前置节点的列表
     * @param visitedNodeIds  用于防止循环引用的已访问节点ID集合
     * @param filterType      要筛选的插件类型，如果为 null，则不进行类型筛选
     * @author fxbin
     */
    private void findPreviousNodesRecursive(Long flowId, String currentNodeId, List<FlowEdge> allEdges, List<FlowNode> previousNodes, Set<String> visitedNodeIds, PluginType filterType) {
        if (!visitedNodeIds.add(currentNodeId)) {
            // 如果已经访问过此节点（在当前递归路径上），则停止以防止循环
            // 注意：对于查找所有前置节点，一个节点可能通过不同路径被多次访问作为"当前节点"，
            // 但我们只关心它作为"前置节点"被添加一次。
            // visitedNodeIds 在这里主要用于防止因环路导致的无限递归。
            // 实际添加到 previousNodes 列表的控制在外部调用处或收集结果时处理重复。
        }

        List<FlowEdge> directPreviousEdges = allEdges.stream()
                .filter(edge -> edge.getTargetNodeId().equals(currentNodeId))
                .collect(Collectors.toList());

        for (FlowEdge edge : directPreviousEdges) {
            String prevNodeId = edge.getSourceNodeId();
            // 检查是否已经作为前置节点添加过，避免重复添加同一个前置节点
            boolean alreadyAdded = previousNodes.stream().anyMatch(node -> node.getId().equals(prevNodeId));
            if (!alreadyAdded) {
                FlowNode prevNode = FlowNodeCache.getNode(flowId, prevNodeId);
                if (prevNode != null) {
                    if (filterType == null || prevNode.getNodeType() == filterType) {
                        previousNodes.add(prevNode);
                    }
                    // 继续递归查找这个前置节点的前置节点
                    findPreviousNodesRecursive(flowId, prevNodeId, allEdges, previousNodes, visitedNodeIds, filterType);
                }
            }
        }
    }

    /**
     * 保存状态
     * 将当前上下文状态持久化到存储
     * @throws ServiceException 当持久化失败时抛出
     */
    public void saveState() {
        try {
            log.info("Saving state for flow: {}, execution: {}", flowId, executionId);

            Map<String, Object> contextData = Map.of(
                    "variables", variables,
                    "startTime", startTime,
                    "version", VERSION,
                    "userId", userId,
                    "tenantId", tenantId
            );

            String serializedContext = STATE_SERIALIZER.serialize(contextData);

            // 1. 使用 L1 Cache (Caffeine)
            String l1CacheKey = L1_CACHE.generateCacheKey(flowId, executionId);
            L1_CACHE.put(l1CacheKey, serializedContext);
            log.debug("State for flow: {}, execution: {} saved to L1 Cache (Caffeine).", flowId, executionId);

            // 2. 将状态写入 L2 Cache (Redis)
            // TODO: 考虑将 Redis 操作异步化以提高性能，例如使用 @Async 或消息队列
            try {
                if (L2_CACHE != null) {
                    String l2CacheKey = L2_CACHE.generateCacheKey(flowId, executionId);
                    // L2_CACHE.put(l2CacheKey, serializedContext, DEFAULT_REDIS_EXPIRE_HOURS * 3600); // Assuming put supports TTL in seconds
                    // Implementations handle their default TTL or specific TTL methods
                    L2_CACHE.put(l2CacheKey, serializedContext);
                    log.info("State for flow: {}, execution: {} also saved to L2 Cache (Redis).", flowId, executionId);
                }
            } catch (Exception redisEx) {
                // Redis 写入失败通常不应阻塞主流程，记录错误即可
                log.error("Failed to save flow state to L2 Cache (Redis) for flow: {}, execution: {}. Error: {}", flowId, executionId, redisEx.getMessage(), redisEx);
            }
        } catch (SerializationException se) {
            log.error("Serialization failed for flow: {}, execution: {}. Error: {}", flowId, executionId, se.getMessage(), se);
            throw new ServiceException("Failed to serialize flow state", se);
        } catch (Exception e) {
            log.error("Failed to save flow state for flow: {}, execution: {}. Error: {}", flowId, executionId, e.getMessage(), e);
            throw new ServiceException("Failed to save flow state", e);
        }
    }

    /**
     * 加载状态
     * 从存储中加载指定流程ID的上下文状态
     *
     * @param flowId 流程ID
     * @param executionId 执行ID（可选）
     * @return 上下文实例
     * @throws ServiceException 当加载失败或数据损坏时抛出
     */
    public static FlowContextHolder loadState(Long flowId, String executionId) {
        try {
            log.info("Loading state for flow: {}, execution: {}", flowId, executionId);

            String l1CacheKey = L1_CACHE.generateCacheKey(flowId, executionId);
            String serializedContext = L1_CACHE.get(l1CacheKey).orElse(null);

            if (serializedContext != null) {
                log.info("Loaded state from L1 Cache (Caffeine) for flow: {}, execution: {}", flowId, executionId);
            } else {
                log.info("State not found in L1 Cache (Caffeine) for flow: {}, execution: {}. Trying L2 Cache (Redis).", flowId, executionId);
                if (L2_CACHE != null) {
                    try {
                        String l2CacheKey = L2_CACHE.generateCacheKey(flowId, executionId);
                        serializedContext = L2_CACHE.get(l2CacheKey).orElse(null);

                        if (serializedContext != null) {
                            log.info("Loaded state from L2 Cache (Redis) for flow: {}, execution: {}. Caching to L1 Cache (Caffeine).", flowId, executionId);
                            // 回填到 L1 Cache
                            L1_CACHE.put(l1CacheKey, serializedContext);
                        } else {
                            log.warn("State not found in L2 Cache (Redis) for flow: {}, execution: {}", flowId, executionId);
                            // Fall through to throw ServiceException if not found in any cache
                        }
                    } catch (Exception l2Ex) {
                        log.error("Failed to load flow state from L2 Cache (Redis) for flow: {}, execution: {}. Error: {}", flowId, executionId, l2Ex.getMessage(), l2Ex);
                        // 根据策略决定是否抛出异常或尝试其他恢复机制
                        // 此处不立即抛出，允许后续检查 serializedContext 是否为 null
                    }
                }
            }

            if (serializedContext == null) {
                throw new ServiceException("No saved state found for flow: " + flowId + " and execution: " + executionId + " in any cache (L1/L2).");
            }

            Map<String, Object> contextData = STATE_SERIALIZER.deserialize(serializedContext);
            if (contextData == null) {
                 throw new ServiceException("Deserialized context data is null for flow: " + flowId + " and execution: " + executionId + ". Serialized data might be corrupt or empty.");
            }

            FlowContextHolder holder = new FlowContextHolder(flowId, (long) contextData.getOrDefault("startTime", SystemClock.now()), executionId, Maps.newConcurrentMap());
            Object variablesMap = contextData.get("variables");
            if (variablesMap instanceof Map) {
                holder.variables.putAll((Map<String, Object>) variablesMap);
            } else if (variablesMap != null) {
                log.warn("Variables in deserialized context is not a Map for flow: {}, execution: {}. Variables: {}", flowId, executionId, variablesMap);
            }

            // 设置用户ID和租户ID
            holder.userId = (String) contextData.get("userId");
            holder.tenantId = (String) contextData.get("tenantId");

            // 检查版本兼容性
            int version = ((Number) contextData.getOrDefault("version", 0)).intValue();
            if (version > VERSION) {
                log.warn("Loading context with newer version: {} (current version: {}) for flow: {}, execution: {}", version, VERSION, flowId, executionId);
            }

            return holder;
        } catch (SerializationException se) {
            log.error("Deserialization failed for flow: {}, execution: {}. Error: {}", flowId, executionId, se.getMessage(), se);
            throw new ServiceException("Failed to deserialize flow state", se);
        } catch (Exception e) {
            log.error("Failed to load flow state for flow: {}, execution: {}. Error: {}", flowId, executionId, e.getMessage(), e);
            throw new ServiceException("Failed to load flow state", e);
        }
    }
}