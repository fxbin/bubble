package cn.fxbin.bubble.flow.core.builder;

import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.flow.core.algorithm.DependencyGraph;
import cn.fxbin.bubble.flow.core.algorithm.ParallelGroupAnalyzer;
import cn.fxbin.bubble.flow.core.config.FlowProperties;
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
 * <p>流程表达式构建器，负责将流程节点和边转换为 LiteFlow EL 表达式。
 * 支持自动识别并行组，生成优化的执行链路。
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

    private final FlowProperties flowProperties;

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
     *
     * @param nodes 节点列表
     * @param edges 边列表
     * @return 流程表达式
     */
    public String buildExpression(List<FlowNode> nodes, List<FlowEdge> edges) {
        DependencyGraph graph = buildDependencyGraph(nodes, edges);

        parallelGroupAnalyzer.analyzeAndAssignGroups(edges, nodes);

        FlowNode startNode = nodes.stream()
                .filter(n -> PluginType.START_NODE == n.getNodeType())
                .findFirst()
                .orElseThrow(() -> new ServiceException("流程缺少开始节点"));
        List<String> sortedNodeIds = graph.topologicalSortFromNode(startNode.getId());

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

        Map<String, FlowNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(FlowNode::getId, node -> node));

        Map<String, List<FlowEdge>> outEdgeMap = edges.stream()
                .collect(Collectors.groupingBy(FlowEdge::getSourceNodeId));

        int maxWaitSeconds = flowProperties.getExecution().getMaxWaitSeconds();
        boolean ignoreError = flowProperties.getExecution().isIgnoreError();

        ThenELWrapper el = ELBus.then(buildNode(nodeMap.get(sortedNodeIds.get(0))));

        List<String> processedTargetNodes = new ArrayList<>();
        processedTargetNodes.add(sortedNodeIds.get(0));

        for (int i = 0; i < sortedNodeIds.size(); i++) {
            String currentNodeId = sortedNodeIds.get(i);

            if (i == sortedNodeIds.size() - 1) {
                break;
            }

            List<FlowEdge> outEdges = outEdgeMap.getOrDefault(currentNodeId, Collections.emptyList());
            if (outEdges.isEmpty()) {
                continue;
            }

            Map<String, List<FlowEdge>> groupedEdges = outEdges.stream()
                    .filter(edge -> edge.getParallelGroup() != null && !edge.getParallelGroup().isEmpty())
                    .collect(Collectors.groupingBy(FlowEdge::getParallelGroup));

            List<FlowEdge> nonParallelEdges = outEdges.stream()
                    .filter(edge -> edge.getParallelGroup() == null || edge.getParallelGroup().isEmpty())
                    .toList();

            List<ELWrapper> nextSteps = new ArrayList<>();

            groupedEdges.forEach((groupId, parallelEdges) -> {
                List<FlowEdge> unprocessedEdges = parallelEdges.stream()
                        .filter(edge -> !processedTargetNodes.contains(edge.getTargetNodeId()))
                        .toList();

                if (!unprocessedEdges.isEmpty()) {
                    unprocessedEdges.forEach(edge -> processedTargetNodes.add(edge.getTargetNodeId()));

                    if (unprocessedEdges.size() > 1) {
                        String[] parallelNodeIds = unprocessedEdges.stream()
                                .map(FlowEdge::getTargetNodeId)
                                .toArray(String[]::new);

                        List<FlowNode> parallelNodes = Arrays.stream(parallelNodeIds)
                                .map(nodeMap::get)
                                .collect(Collectors.toList());

                        nextSteps.add(ELBus.when(buildNode(parallelNodes))
                                .maxWaitSeconds(maxWaitSeconds)
                                .ignoreError(ignoreError));
                    } else {
                        nextSteps.add(ELBus.then(buildNode(nodeMap.get(unprocessedEdges.get(0).getTargetNodeId())))
                                .maxWaitSeconds(maxWaitSeconds));
                    }
                }
            });

            for (FlowEdge edge : nonParallelEdges) {
                String targetNodeId = edge.getTargetNodeId();
                if (!processedTargetNodes.contains(targetNodeId)) {
                    processedTargetNodes.add(targetNodeId);
                    nextSteps.add(ELBus.then(buildNode(nodeMap.get(targetNodeId)))
                            .maxWaitSeconds(maxWaitSeconds));
                }
            }

            if (nextSteps.size() == 1) {
                el.then(nextSteps.get(0));
            } else if (nextSteps.size() > 1) {
                Object[] nextStepObjects = nextSteps.toArray();
                el.then(ELBus.when(nextStepObjects)
                        .maxWaitSeconds(maxWaitSeconds)
                        .ignoreError(ignoreError));
            }
        }

        if (!sortedNodeIds.isEmpty()) {
            String lastNodeId = sortedNodeIds.get(sortedNodeIds.size() - 1);
            if (!processedTargetNodes.contains(lastNodeId)) {
                el.then(buildNode(nodeMap.get(lastNodeId)));
            }
        }

        log.debug("生成的流程表达式: {}", el.toEL());

        return el.toEL();
    }

    private DependencyGraph buildDependencyGraph(List<FlowNode> nodes, List<FlowEdge> edges) {
        DependencyGraph graph = new DependencyGraph();
        nodes.forEach(node -> graph.addNode(node.getId()));
        edges.forEach(edge -> graph.addEdge(edge.getSourceNodeId(), edge.getTargetNodeId()));
        return graph;
    }

    private NodeELWrapper[] buildNode(List<FlowNode> flowNodeList) {
        return flowNodeList.stream().map(this::buildNode).toArray(NodeELWrapper[]::new);
    }

    private NodeELWrapper buildNode(FlowNode flowNode) {
        return (NodeELWrapper) new NodeELWrapper(flowNode.getNodeType().getName()).tag(flowNode.getId());
    }

}
