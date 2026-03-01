package cn.fxbin.bubble.flow.core.cache;

import cn.fxbin.bubble.core.util.ObjectUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.data.redis.RedisOperations;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import cn.fxbin.bubble.flow.core.model.dto.FlowDefinitionDTO;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.fxbin.bubble.flow.core.provider.FlowDefinitionProvider;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FlowNodeCache
 *
 * <p>流程节点缓存组件，提供节点和边的缓存查询能力。
 * 基于 Redis 实现分布式缓存，支持跨实例数据共享。
 *
 * <p>缓存结构：
 * <ul>
 *   <li>节点缓存：flow:{flowId}:nodes - Hash 结构，key 为 nodeId</li>
 *   <li>边缓存：flow:{flowId}:edges - String 结构，存储边列表</li>
 * </ul>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/5/7 15:21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowNodeCache {

    private static final Long CACHE_EXPIRE_SECONDS = 3600L;

    private final RedisOperations redisOperations;

    private final FlowDefinitionProvider flowDefinitionProvider;

    private interface FlowNodeCacheKey {
        String FLOW_NODE_CACHE_KEY = "flow:{}:nodes";
        String FLOW_NODE_EDGE_CACHE_KEY = "flow:{}:edges";
    }

    /**
     * 加载指定流程的所有节点和边到缓存
     *
     * @param flowId 流程ID
     */
    public void loadAllNodeByFlowId(Long flowId) {
        FlowDefinitionDTO flowDetail = flowDefinitionProvider.getFlowDetail(flowId);
        FlowChain schema = flowDetail.getSchema();
        List<FlowNode> nodes = schema.getNodes();
        Map<String, FlowNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(FlowNode::getId, node -> node));

        String nodeCacheKey = getFlowNodeCacheKey(flowId);
        String edgeCacheKey = getFlowNodeEdgeCacheKey(flowId);

        redisOperations.hmset(nodeCacheKey, nodeMap, CACHE_EXPIRE_SECONDS);
        redisOperations.set(edgeCacheKey, schema.getEdges(), CACHE_EXPIRE_SECONDS);
    }

    /**
     * 获取指定流程的所有节点
     *
     * @param flowId 流程ID
     * @return 节点列表，如果缓存未命中则从数据库加载
     */
    public List<FlowNode> getAllNodes(Long flowId) {
        String nodeCacheKey = getFlowNodeCacheKey(flowId);
        Map<String, FlowNode> flowNodeMap = (Map<String, FlowNode>) redisOperations.hmget(nodeCacheKey);
        if (ObjectUtils.isNotEmpty(flowNodeMap)) {
            return flowNodeMap.values().stream().toList();
        }
        FlowDefinitionDTO flowDetail = flowDefinitionProvider.getFlowDetail(flowId);
        if (flowDetail != null && flowDetail.getSchema() != null) {
            List<FlowNode> nodes = flowDetail.getSchema().getNodes();
            if (nodes != null) {
                Map<String, FlowNode> nodeMap = nodes.stream()
                        .collect(Collectors.toMap(FlowNode::getId, node -> node));
                redisOperations.hmset(nodeCacheKey, nodeMap, CACHE_EXPIRE_SECONDS);
                return nodes;
            }
        }
        return Lists.newArrayList();
    }

    /**
     * 获取指定流程的指定节点
     *
     * @param flowId 流程ID
     * @param nodeId 节点ID
     * @return 节点对象，如果不存在返回 null
     */
    public FlowNode getNode(Long flowId, String nodeId) {
        return (FlowNode) redisOperations.hget(getFlowNodeCacheKey(flowId), nodeId);
    }

    /**
     * 获取指定流程的所有边信息
     *
     * @param flowId 流程ID
     * @return 流程边列表
     */
    @SuppressWarnings("unchecked")
    public List<FlowEdge> getAllEdges(Long flowId) {
        String edgeCacheKey = getFlowNodeEdgeCacheKey(flowId);
        Object cachedEdges = redisOperations.get(edgeCacheKey);
        if (cachedEdges instanceof List) {
            return (List<FlowEdge>) cachedEdges;
        }

        log.warn("Edges for flowId {} not found in cache or incorrect type.", flowId);
        FlowDefinitionDTO flowDetail = flowDefinitionProvider.getFlowDetail(flowId);
        if (flowDetail != null && flowDetail.getSchema() != null) {
            List<FlowEdge> edges = flowDetail.getSchema().getEdges();
            if (edges != null) {
                redisOperations.set(edgeCacheKey, edges, CACHE_EXPIRE_SECONDS);
                return edges;
            }
        }
        return Collections.emptyList();
    }

    private String getFlowNodeCacheKey(Long flowId) {
        return StringUtils.format(FlowNodeCacheKey.FLOW_NODE_CACHE_KEY, flowId);
    }

    private String getFlowNodeEdgeCacheKey(Long flowId) {
        return StringUtils.format(FlowNodeCacheKey.FLOW_NODE_EDGE_CACHE_KEY, flowId);
    }

}
