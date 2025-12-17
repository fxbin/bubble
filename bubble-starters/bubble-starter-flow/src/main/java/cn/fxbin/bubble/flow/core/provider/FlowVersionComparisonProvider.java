package cn.fxbin.bubble.flow.core.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.flow.core.mapper.FlowVersionHistoryMapper;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.fxbin.bubble.flow.core.model.entity.FlowVersionHistory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程版本对比服务
 * <p>提供版本间的差异分析、对比报告生成等功能</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowVersionComparisonProvider {

    private final FlowVersionHistoryMapper flowVersionHistoryMapper;

    /**
     * 版本差异对比结果
     */
    @Getter
    public static class VersionDifference {
        private final Integer fromVersion;
        private final Integer toVersion;
        private final List<NodeDifference> nodeDifferences;
        private final List<EdgeDifference> edgeDifferences;
        private final Map<String, Object> metadataDifferences;

        public VersionDifference(Integer fromVersion, Integer toVersion) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.nodeDifferences = new ArrayList<>();
            this.edgeDifferences = new ArrayList<>();
            this.metadataDifferences = new HashMap<>();
        }
    }

    /**
     * 节点差异
     */
    @Getter
    public static class NodeDifference {
        private final String nodeId;
        private final DifferenceType type;
        private final FlowNode oldNode;
        private final FlowNode newNode;
        private final Map<String, Object> propertyChanges;

        public NodeDifference(String nodeId, DifferenceType type, FlowNode oldNode, FlowNode newNode) {
            this.nodeId = nodeId;
            this.type = type;
            this.oldNode = oldNode;
            this.newNode = newNode;
            this.propertyChanges = new HashMap<>();
        }

    }

    /**
     * 边差异
     */
    @Getter
    public static class EdgeDifference {
        private final Long edgeId;
        private final DifferenceType type;
        private final FlowEdge oldEdge;
        private final FlowEdge newEdge;

        public EdgeDifference(Long edgeId, DifferenceType type, FlowEdge oldEdge, FlowEdge newEdge) {
            this.edgeId = edgeId;
            this.type = type;
            this.oldEdge = oldEdge;
            this.newEdge = newEdge;
        }

    }

    /**
     * 差异类型
     */
    public enum DifferenceType {
        // 新增
        ADDED,
        // 删除
        REMOVED,
        // 修改
        MODIFIED
    }

    /**
     * 对比两个版本的差异
     * <p>分析节点、边和元数据的变化</p>
     *
     * @param flowId 流程ID
     * @param fromVersion 源版本号
     * @param toVersion 目标版本号
     * @return 版本差异对比结果
     * @throws ServiceException 如果版本不存在
     */
    public VersionDifference compareVersions(Long flowId, Integer fromVersion, Integer toVersion) {
        log.info("Comparing flow versions: flowId={}, from={}, to={}", flowId, fromVersion, toVersion);

        // 1. 获取两个版本的快照
        FlowVersionHistory fromHistory = getVersionHistory(flowId, fromVersion);
        FlowVersionHistory toHistory = getVersionHistory(flowId, toVersion);

        FlowChain fromSnapshot = fromHistory.getSnapshot();
        FlowChain toSnapshot = toHistory.getSnapshot();

        // 2. 创建差异对比结果
        VersionDifference difference = new VersionDifference(fromVersion, toVersion);

        // 3. 对比节点差异
        compareNodes(fromSnapshot.getNodes(), toSnapshot.getNodes(), difference);

        // 4. 对比边差异
        compareEdges(fromSnapshot.getEdges(), toSnapshot.getEdges(), difference);

        // 5. 对比元数据差异
        compareMetadata(fromHistory, toHistory, difference);

        log.info("Version comparison completed: {} node differences, {} edge differences", 
                difference.getNodeDifferences().size(), difference.getEdgeDifferences().size());

        return difference;
    }

    /**
     * 获取版本历史记录
     */
    private FlowVersionHistory getVersionHistory(Long flowId, Integer version) {
        FlowVersionHistory history = flowVersionHistoryMapper.selectOne(
            new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .eq(FlowVersionHistory::getVersion, version)
        );
        
        if (history == null) {
            throw new ServiceException(String.format("Version history not found: flowId=%d, version=%d", flowId, version));
        }
        
        return history;
    }

    /**
     * 对比节点差异
     */
    private void compareNodes(List<FlowNode> fromNodes, List<FlowNode> toNodes, VersionDifference difference) {
        Map<String, FlowNode> fromNodeMap = fromNodes.stream()
            .collect(Collectors.toMap(FlowNode::getId, node -> node));
        Map<String, FlowNode> toNodeMap = toNodes.stream()
            .collect(Collectors.toMap(FlowNode::getId, node -> node));

        // 查找新增的节点
        for (FlowNode toNode : toNodes) {
            if (!fromNodeMap.containsKey(toNode.getId())) {
                difference.getNodeDifferences().add(
                    new NodeDifference(toNode.getId(), DifferenceType.ADDED, null, toNode)
                );
            }
        }

        // 查找删除和修改的节点
        for (FlowNode fromNode : fromNodes) {
            String nodeId = fromNode.getId();
            if (!toNodeMap.containsKey(nodeId)) {
                // 节点被删除
                difference.getNodeDifferences().add(
                    new NodeDifference(nodeId, DifferenceType.REMOVED, fromNode, null)
                );
            } else {
                // 检查节点是否被修改
                FlowNode toNode = toNodeMap.get(nodeId);
                if (!nodesEqual(fromNode, toNode)) {
                    NodeDifference nodeDiff = new NodeDifference(nodeId, DifferenceType.MODIFIED, fromNode, toNode);
                    analyzeNodePropertyChanges(fromNode, toNode, nodeDiff);
                    difference.getNodeDifferences().add(nodeDiff);
                }
            }
        }
    }

    /**
     * 对比边差异
     */
    private void compareEdges(List<FlowEdge> fromEdges, List<FlowEdge> toEdges, VersionDifference difference) {
        Map<Long, FlowEdge> fromEdgeMap = fromEdges.stream()
            .collect(Collectors.toMap(FlowEdge::getId, edge -> edge));
        Map<Long, FlowEdge> toEdgeMap = toEdges.stream()
            .collect(Collectors.toMap(FlowEdge::getId, edge -> edge));

        // 查找新增的边
        for (FlowEdge toEdge : toEdges) {
            if (!fromEdgeMap.containsKey(toEdge.getId())) {
                difference.getEdgeDifferences().add(
                    new EdgeDifference(toEdge.getId(), DifferenceType.ADDED, null, toEdge)
                );
            }
        }

        // 查找删除和修改的边
        for (FlowEdge fromEdge : fromEdges) {
            Long edgeId = fromEdge.getId();
            if (!toEdgeMap.containsKey(edgeId)) {
                // 边被删除
                difference.getEdgeDifferences().add(
                    new EdgeDifference(edgeId, DifferenceType.REMOVED, fromEdge, null)
                );
            } else {
                // 检查边是否被修改
                FlowEdge toEdge = toEdgeMap.get(edgeId);
                if (!edgesEqual(fromEdge, toEdge)) {
                    difference.getEdgeDifferences().add(
                        new EdgeDifference(edgeId, DifferenceType.MODIFIED, fromEdge, toEdge)
                    );
                }
            }
        }
    }

    /**
     * 对比元数据差异
     */
    private void compareMetadata(FlowVersionHistory fromHistory, FlowVersionHistory toHistory, VersionDifference difference) {
        if (!Objects.equals(fromHistory.getName(), toHistory.getName())) {
            difference.getMetadataDifferences().put("name", 
                Map.of("from", fromHistory.getName(), "to", toHistory.getName()));
        }
        
        if (!Objects.equals(fromHistory.getDescription(), toHistory.getDescription())) {
            difference.getMetadataDifferences().put("description", 
                Map.of("from", fromHistory.getDescription(), "to", toHistory.getDescription()));
        }
    }

    /**
     * 检查两个节点是否相等
     */
    private boolean nodesEqual(FlowNode node1, FlowNode node2) {
        return Objects.equals(node1.getName(), node2.getName()) &&
               Objects.equals(node1.getNodeType(), node2.getNodeType()) &&
               Objects.equals(node1.getConfig(), node2.getConfig()) &&
               Objects.equals(node1.getPosition(), node2.getPosition());
    }

    /**
     * 检查两个边是否相等
     */
    private boolean edgesEqual(FlowEdge edge1, FlowEdge edge2) {
        return Objects.equals(edge1.getSourceNodeId(), edge2.getSourceNodeId()) &&
               Objects.equals(edge1.getTargetNodeId(), edge2.getTargetNodeId()); /*&&*/
//               Objects.equals(edge1.getCondition(), edge2.getCondition());
    }

    /**
     * 分析节点属性变化
     */
    private void analyzeNodePropertyChanges(FlowNode fromNode, FlowNode toNode, NodeDifference nodeDiff) {
        // 检查名称变化
        if (!Objects.equals(fromNode.getName(), toNode.getName())) {
            nodeDiff.getPropertyChanges().put("name", 
                Map.of("from", fromNode.getName(), "to", toNode.getName()));
        }
        
        // 检查类型变化
        if (!Objects.equals(fromNode.getNodeType(), toNode.getNodeType())) {
            nodeDiff.getPropertyChanges().put("type", 
                Map.of("from", fromNode.getNodeType(), "to", toNode.getNodeType()));
        }
        
        // 检查配置变化
        if (!Objects.equals(fromNode.getConfig(), toNode.getConfig())) {
            nodeDiff.getPropertyChanges().put("config", 
                Map.of("from", fromNode.getConfig(), "to", toNode.getConfig()));
        }

        // 检查位置变化
        if (!Objects.equals(fromNode.getPosition(), toNode.getPosition())) {
            nodeDiff.getPropertyChanges().put("position", 
                Map.of("from", fromNode.getPosition(), "to", toNode.getPosition()));
        }
    }

    /**
     * 生成版本对比报告
     * <p>将差异分析结果格式化为可读的报告</p>
     *
     * @param difference 版本差异对比结果
     * @return 格式化的对比报告
     */
    public String generateComparisonReport(VersionDifference difference) {
        StringBuilder report = new StringBuilder();
        
        report.append(String.format("=== 版本对比报告: V%d -> V%d ===\n\n", 
                     difference.getFromVersion(), difference.getToVersion()));
        
        // 节点变化统计
        long addedNodes = difference.getNodeDifferences().stream()
            .filter(diff -> diff.getType() == DifferenceType.ADDED).count();
        long removedNodes = difference.getNodeDifferences().stream()
            .filter(diff -> diff.getType() == DifferenceType.REMOVED).count();
        long modifiedNodes = difference.getNodeDifferences().stream()
            .filter(diff -> diff.getType() == DifferenceType.MODIFIED).count();
        
        report.append(String.format("节点变化: 新增 %d, 删除 %d, 修改 %d\n", 
                     addedNodes, removedNodes, modifiedNodes));
        
        // 边变化统计
        long addedEdges = difference.getEdgeDifferences().stream()
            .filter(diff -> diff.getType() == DifferenceType.ADDED).count();
        long removedEdges = difference.getEdgeDifferences().stream()
            .filter(diff -> diff.getType() == DifferenceType.REMOVED).count();
        long modifiedEdges = difference.getEdgeDifferences().stream()
            .filter(diff -> diff.getType() == DifferenceType.MODIFIED).count();
        
        report.append(String.format("边变化: 新增 %d, 删除 %d, 修改 %d\n\n", 
                     addedEdges, removedEdges, modifiedEdges));
        
        // 详细变化列表
        if (!difference.getNodeDifferences().isEmpty()) {
            report.append("=== 节点变化详情 ===\n");
            for (NodeDifference nodeDiff : difference.getNodeDifferences()) {
                report.append(String.format("- [%s] 节点 %s: %s\n", 
                             nodeDiff.getType(), nodeDiff.getNodeId(), 
                             getNodeChangeDescription(nodeDiff)));
            }
            report.append("\n");
        }
        
        if (!difference.getEdgeDifferences().isEmpty()) {
            report.append("=== 边变化详情 ===\n");
            for (EdgeDifference edgeDiff : difference.getEdgeDifferences()) {
                report.append(String.format("- [%s] 边 %s: %s\n", 
                             edgeDiff.getType(), edgeDiff.getEdgeId(), 
                             getEdgeChangeDescription(edgeDiff)));
            }
            report.append("\n");
        }
        
        if (!difference.getMetadataDifferences().isEmpty()) {
            report.append("=== 元数据变化 ===\n");
            difference.getMetadataDifferences().forEach((key, value) -> {
                report.append(String.format("- %s: %s\n", key, value));
            });
        }
        
        return report.toString();
    }
    
    /**
     * 获取节点变化描述
     */
    private String getNodeChangeDescription(NodeDifference nodeDiff) {
        switch (nodeDiff.getType()) {
            case ADDED:
                return String.format("新增节点 '%s' (类型: %s)", 
                                   nodeDiff.getNewNode().getName(), nodeDiff.getNewNode().getNodeType().getDesc());
            case REMOVED:
                return String.format("删除节点 '%s' (类型: %s)", 
                                   nodeDiff.getOldNode().getName(), nodeDiff.getOldNode().getNodeType().getDesc());
            case MODIFIED:
                return String.format("修改节点 '%s', 变化: %s", 
                                   nodeDiff.getNewNode().getName(), nodeDiff.getPropertyChanges().keySet());
            default:
                return "未知变化";
        }
    }
    
    /**
     * 获取边变化描述
     */
    private String getEdgeChangeDescription(EdgeDifference edgeDiff) {
        switch (edgeDiff.getType()) {
            case ADDED:
                return String.format("新增连接 %s -> %s", 
                                   edgeDiff.getNewEdge().getSourceNodeId(), edgeDiff.getNewEdge().getTargetNodeId());
            case REMOVED:
                return String.format("删除连接 %s -> %s", 
                                   edgeDiff.getOldEdge().getSourceNodeId(), edgeDiff.getOldEdge().getTargetNodeId());
            case MODIFIED:
                return String.format("修改连接 %s -> %s", 
                                   edgeDiff.getNewEdge().getSourceNodeId(), edgeDiff.getNewEdge().getTargetNodeId());
            default:
                return "未知变化";
        }
    }
}