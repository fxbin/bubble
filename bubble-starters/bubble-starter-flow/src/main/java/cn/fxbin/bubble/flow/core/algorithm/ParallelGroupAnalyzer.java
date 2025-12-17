package cn.fxbin.bubble.flow.core.algorithm;

import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ParallelGroupAnalyzer
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:27
 */
public class ParallelGroupAnalyzer {

    /**
     * 自动识别可并行边并分配组ID
     */
    public void analyzeAndAssignGroups(List<FlowEdge> edges, List<FlowNode> nodes) {
        // 构建依赖图
        DependencyGraph graph = new DependencyGraph();
        nodes.forEach(node -> graph.addNode(node.getId()));
        edges.forEach(edge -> graph.addEdge(edge.getSourceNodeId(), edge.getTargetNodeId()));

        // 按源节点分组
        Map<String, List<FlowEdge>> edgesBySource = edges.stream()
                .collect(Collectors.groupingBy(FlowEdge::getSourceNodeId));

        edgesBySource.forEach((source, edgesFromSource) -> {
            // 找出可并行执行的边
            List<FlowEdge> parallelEdges = findParallelEdges(edgesFromSource, graph);

            // 分配并行组ID
            if (parallelEdges.size() > 1) {
                String groupId = "PG_" + StringUtils.getUUID();
                parallelEdges.forEach(edge -> edge.setParallelGroup(groupId));
            } else {
                // 如果只有一条边，则不分组
                parallelEdges.forEach(edge -> edge.setParallelGroup(""));
            }
        });
    }

    /**
     * 找出同一源节点下无互相依赖的边
     */
    private List<FlowEdge> findParallelEdges(List<FlowEdge> edges, DependencyGraph graph) {
        List<FlowEdge> candidates = new ArrayList<>(edges);
        List<FlowEdge> parallelEdges = new ArrayList<>();

        while (!candidates.isEmpty()) {
            FlowEdge current = candidates.remove(0);
            parallelEdges.add(current);

            // 使用 graph.hasDependency 判断依赖关系
            Iterator<FlowEdge> iterator = candidates.iterator();
            while (iterator.hasNext()) {
                FlowEdge other = iterator.next();
                if (graph.hasDependency(current.getTargetNodeId(), other.getTargetNodeId())) {
                    iterator.remove();
                }
            }
        }
        return parallelEdges;
    }
    
}
