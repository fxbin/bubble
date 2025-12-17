package cn.fxbin.bubble.flow.core.builder;

import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.flow.core.algorithm.DependencyGraph;
import cn.fxbin.bubble.flow.core.algorithm.ParallelGroupAnalyzer;
import cn.fxbin.bubble.flow.core.enums.PluginType;
import cn.fxbin.bubble.flow.core.mapper.FlowEdgeMapper;
import cn.fxbin.bubble.flow.core.mapper.FlowNodeMapper;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import com.yomahub.liteflow.builder.el.ELBus;
import com.yomahub.liteflow.builder.el.ELWrapper;
import com.yomahub.liteflow.builder.el.NodeELWrapper;
import com.yomahub.liteflow.builder.el.ThenELWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FlowExpressionBuilder
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExpressionBuilder {

    private final FlowNodeMapper flowNodeMapper;
    private final FlowEdgeMapper flowEdgeMapper;

    private final ParallelGroupAnalyzer parallelGroupAnalyzer = new ParallelGroupAnalyzer();

    /**
     * 根据流程ID构建流程表达式
     *
     * @param flowId 流程ID
     * @return 流程表达式
     */
    public String buildExpression(Long flowId) {
        List<FlowNode> nodes = flowNodeMapper.selectList(FlowNode::getFlowId, flowId);
        List<FlowEdge> edges = flowEdgeMapper.selectList(FlowEdge::getFlowId, flowId);
        return buildExpression(nodes, edges);
    }


    /**
     * 根据节点和边列表构建流程表达式
     * @param nodes 节点列表
     * @param edges 边列表
     * @return 流程表达式
     */
    public String buildExpression(List<FlowNode> nodes, List<FlowEdge> edges) {

        // 1. 构建依赖图
        DependencyGraph graph = buildDependencyGraph(nodes, edges);

        // 2. 智能分析并行组
        parallelGroupAnalyzer.analyzeAndAssignGroups(edges, nodes);

        // 3. 从开始节点进行拓扑排序
        FlowNode startNode = nodes.stream()
                .filter(n -> PluginType.START_NODE == n.getNodeType())
                .findFirst()
                .orElseThrow(() -> new ServiceException("流程缺少开始节点"));
        List<String> sortedNodeIds = graph.topologicalSortFromNode(startNode.getId());

        // 4. 使用 LiteFlow EL Builder 生成表达式
        return buildChainExpressionWithElBuilder(sortedNodeIds, edges, nodes);
    }

    /**
     * 使用 LiteFlow EL Builder 生成链式表达式
     *
     * @param sortedNodeIds 拓扑排序后的节点ID列表
     * @param edges         边列表
     * @param nodes         节点列表 (用于获取节点信息)
     * @return LiteFlow EL 表达式字符串
     */
    private String buildChainExpressionWithElBuilder(List<String> sortedNodeIds, List<FlowEdge> edges, List<FlowNode> nodes) {
        if (sortedNodeIds.isEmpty()) {
            return "";
        }

        // 将节点按ID存入Map，方便查找
        Map<String, FlowNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(FlowNode::getId, node -> node));

        // 将出边按源节点ID分组
        Map<String, List<FlowEdge>> outEdgeMap = edges.stream()
                .collect(Collectors.groupingBy(FlowEdge::getSourceNodeId));

        // 使用 ELBus 构建表达式
        // 从第一个节点开始
        ThenELWrapper el = ELBus.then(buildNode(nodeMap.get(sortedNodeIds.get(0))));
        
        // 跟踪已处理的节点，避免重复添加
        List<String> processedTargetNodes = new ArrayList<>();
        // 添加第一个节点到已处理列表
        processedTargetNodes.add(sortedNodeIds.get(0));

        // 遍历拓扑排序后的节点列表，构建表达式
        for (int i = 0; i < sortedNodeIds.size(); i++) {
            String currentNodeId = sortedNodeIds.get(i);
            
            // 如果当前节点已经是最后一个节点，不需要处理其出边
            if (i == sortedNodeIds.size() - 1) {
                break;
            }
            
            List<FlowEdge> outEdges = outEdgeMap.getOrDefault(currentNodeId, Collections.emptyList());
            if (outEdges.isEmpty()) {
                continue;
            }
            
            // 按并行组分组
            Map<String, List<FlowEdge>> groupedEdges = outEdges.stream()
                    .filter(edge -> edge.getParallelGroup() != null && !edge.getParallelGroup().isEmpty())
                    .collect(Collectors.groupingBy(FlowEdge::getParallelGroup));

            // 提取非并行组的边
            List<FlowEdge> nonParallelEdges = outEdges.stream()
                    .filter(edge -> edge.getParallelGroup() == null || edge.getParallelGroup().isEmpty())
                    .toList();

            // 构建下一个步骤的表达式元素数组
            List<ELWrapper> nextSteps = new ArrayList<>();

            // 处理并行组
            groupedEdges.forEach((groupId, parallelEdges) -> {
                // 过滤出未处理的节点
                List<FlowEdge> unprocessedEdges = parallelEdges.stream()
                        .filter(edge -> !processedTargetNodes.contains(edge.getTargetNodeId()))
                        .toList();
                
                if (!unprocessedEdges.isEmpty()) {
                    // 将未处理的节点添加到已处理列表中
                    unprocessedEdges.forEach(edge -> processedTargetNodes.add(edge.getTargetNodeId()));
                    
                    if (unprocessedEdges.size() > 1) {
                        // 如果有多个未处理的节点，使用WHEN
                        String[] parallelNodeIds = unprocessedEdges.stream()
                                .map(FlowEdge::getTargetNodeId)
                                .toArray(String[]::new);

                        List<FlowNode> parallelNodes = Arrays.stream(parallelNodeIds)
                                .map(nodeMap::get)
                                .collect(Collectors.toList());

                        nextSteps.add(ELBus.when(buildNode(parallelNodes)).maxWaitSeconds(900).ignoreError(true));
                    } else {
                        // 如果只有一个未处理的节点，直接添加
                        nextSteps.add(ELBus.then(buildNode(nodeMap.get(unprocessedEdges.get(0).getTargetNodeId()))).maxWaitSeconds(900));
                    }
                }
            });

            // 处理非并行边 (顺序执行)
            for (FlowEdge edge : nonParallelEdges) {
                String targetNodeId = edge.getTargetNodeId();
                // 检查目标节点是否已处理，避免重复添加
                if (!processedTargetNodes.contains(targetNodeId)) {
                    processedTargetNodes.add(targetNodeId);
                    nextSteps.add(ELBus.then(buildNode(nodeMap.get(targetNodeId))).maxWaitSeconds(900));
                }
            }

            // 将所有下一步连接到当前节点
            if (nextSteps.size() == 1) {
                // 如果只有一个下一步，直接THEN
                el.then(nextSteps.get(0));
            } else if (nextSteps.size() > 1) {
                // 如果有多个下一步，使用WHEN
                Object[] nextStepObjects = nextSteps.toArray();
                el.then(ELBus.when(nextStepObjects).maxWaitSeconds(900).ignoreError(true));
            }
        }
        
        // 处理最后一个节点（如果有且未处理）
        if (!sortedNodeIds.isEmpty()) {
            String lastNodeId = sortedNodeIds.get(sortedNodeIds.size() - 1);
            if (!processedTargetNodes.contains(lastNodeId)) {
                el.then(buildNode(nodeMap.get(lastNodeId)));
            }
        }
        
        // 打印生成的表达式，用于调试
        log.debug("生成的流程表达式: {}", el.toEL());

        return el.toEL();
    }


    /**
     * 构建依赖图
     */
    private DependencyGraph buildDependencyGraph(List<FlowNode> nodes, List<FlowEdge> edges) {
        DependencyGraph graph = new DependencyGraph();
        nodes.forEach(node -> graph.addNode(node.getId()));
        edges.forEach(edge -> graph.addEdge(edge.getSourceNodeId(), edge.getTargetNodeId()));
        return graph;
    }


    private NodeELWrapper [] buildNode(List<FlowNode> flowNodeList) {
        return flowNodeList.stream().map(this::buildNode).toArray(NodeELWrapper[]::new);
    }

    private NodeELWrapper buildNode(FlowNode flowNode) {
        // NodeId 存放节点ID,
        return (NodeELWrapper) new NodeELWrapper(flowNode.getNodeType().getName()).tag(flowNode.getId());
    }

}
