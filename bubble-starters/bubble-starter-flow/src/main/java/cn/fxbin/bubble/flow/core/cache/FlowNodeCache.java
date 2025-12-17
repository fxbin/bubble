package cn.fxbin.bubble.flow.core.cache;

import cn.fxbin.bubble.core.util.ApplicationContextHolder;
import cn.fxbin.bubble.core.util.ObjectUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.data.redis.RedisOperations;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import cn.fxbin.bubble.flow.core.model.dto.FlowDefinitionDTO;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.fxbin.bubble.flow.core.provider.FlowDefinitionProvider;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FlowNodeCache
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/5/7 15:21
 */
@Slf4j
@UtilityClass
public class FlowNodeCache {

    private final RedisOperations redisOperations = ApplicationContextHolder.getBean(RedisOperations.class);

    private final FlowDefinitionProvider flowDefinitionProvider = ApplicationContextHolder.getBean(FlowDefinitionProvider.class);

    private final Long CACHE_EXPIRE_SECONDS = 3600L;


    interface FlowNodeCacheKey {
        String FLOW_NODE_CACHE_KEY = "flow:{}:nodes";
        String FLOW_NODE_EDGE_CACHE_KEY = "flow:{}:edges";
    }


    public void loadAllNodeByFlowId(Long flowId) {

        FlowDefinitionDTO flowDetail = flowDefinitionProvider.getFlowDetail(flowId);
        FlowChain schema = flowDetail.getSchema();
        List<FlowNode> nodes = schema.getNodes();
        Map<String, FlowNode> nodeMap = nodes.stream().collect(Collectors.toMap(FlowNode::getId, node -> node));

        String nodeCacheKey = getFlowNodeCacheKey(flowId);
        String edgeCacheKey = getFlowNodeEdgeCacheKey(flowId);

        redisOperations.hmset(nodeCacheKey, nodeMap, CACHE_EXPIRE_SECONDS);
        redisOperations.set(edgeCacheKey, schema.getEdges(), CACHE_EXPIRE_SECONDS);

    }

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
                // 重新缓存
                Map<String, FlowNode> nodeMap = nodes.stream().collect(Collectors.toMap(FlowNode::getId, node -> node));
                redisOperations.hmset(nodeCacheKey, nodeMap, CACHE_EXPIRE_SECONDS);
                return nodes;
            }
        }
        return Lists.newArrayList();
    }

    public FlowNode getNode(Long flowId, String nodeId) {
        return (FlowNode) redisOperations.hget(getFlowNodeCacheKey(flowId), nodeId);
    }

    /**
     * 获取指定流程的所有边信息
     *
     * @param flowId 流程ID
     * @return 流程边列表
     * @author fxbin
     */
    @SuppressWarnings("unchecked")
    public List<FlowEdge> getAllEdges(Long flowId) {
        String edgeCacheKey = getFlowNodeEdgeCacheKey(flowId);
        Object cachedEdges = redisOperations.get(edgeCacheKey);
        if (cachedEdges instanceof List) {
            return (List<FlowEdge>) cachedEdges;
        }

        // 如果类型不匹配或为null，可以考虑从 flowDefinitionProvider 重新加载
        log.warn("Edges for flowId {} not found in cache or incorrect type.", flowId);
        FlowDefinitionDTO flowDetail = flowDefinitionProvider.getFlowDetail(flowId);
        if (flowDetail != null && flowDetail.getSchema() != null) {
             List<FlowEdge> edges = flowDetail.getSchema().getEdges();
             if (edges != null) {
                 // 重新缓存
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
