package cn.fxbin.bubble.flow.core.algorithm;

import cn.fxbin.bubble.flow.core.exception.CycleDetectedException;

import java.util.*;

/**
 * 依赖关系图
 *
 * <p>用于管理工作流节点间的依赖关系，支持依赖分析和循环依赖检测。
 * 基于 Kahn 算法实现拓扑排序，能够检测循环依赖。
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:22
 */
public class DependencyGraph {

    /**
     * 邻接表，存储节点的直接后继节点
     * key: 当前节点ID
     * value: 当前节点的所有直接后继节点ID集合
     */
    private final Map<String, Set<String>> adjacencyList = new HashMap<>();

    /**
     * 反向邻接表，存储节点的直接前驱节点
     * key: 当前节点ID
     * value: 当前节点的所有直接前驱节点ID集合
     */
    private final Map<String, Set<String>> reverseAdjacencyList = new HashMap<>();

    /**
     * 添加节点到依赖图中
     * 为正向和反向邻接表初始化空的依赖集合
     *
     * @param nodeId 待添加的节点ID
     */
    public void addNode(String nodeId) {
        adjacencyList.putIfAbsent(nodeId, new HashSet<>());
        reverseAdjacencyList.putIfAbsent(nodeId, new HashSet<>());
    }

    /**
     * 添加节点间的依赖关系
     * 同时更新正向和反向邻接表
     *
     * @param source 源节点ID（依赖方）
     * @param target 目标节点ID（被依赖方）
     */
    public void addEdge(String source, String target) {
        adjacencyList.get(source).add(target);
        reverseAdjacencyList.get(target).add(source);
    }

    /**
     * 获取指定节点的所有祖先节点（所有上游依赖节点）
     * 使用广度优先搜索遍历反向邻接表获取所有前驱节点
     *
     * @param nodeId 目标节点ID
     * @return 包含所有祖先节点ID的集合
     */
    public Set<String> getAllAncestors(String nodeId) {
        Set<String> ancestors = new HashSet<>();
        Queue<String> queue = new LinkedList<>(reverseAdjacencyList.get(nodeId));
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (ancestors.add(current)) {
                queue.addAll(reverseAdjacencyList.get(current));
            }
        }
        return ancestors;
    }

    /**
     * 检查两个节点之间是否存在依赖关系
     * 通过分析两个节点的祖先节点集合来判断是否存在直接或间接依赖
     *
     * @param nodeA 第一个节点ID
     * @param nodeB 第二个节点ID
     * @return 如果存在依赖关系返回true，否则返回false
     */
    public boolean hasDependency(String nodeA, String nodeB) {
        Set<String> ancestorsA = getAllAncestors(nodeA);
        Set<String> ancestorsB = getAllAncestors(nodeB);
        return ancestorsA.contains(nodeB) || ancestorsB.contains(nodeA);
    }

    /**
     * 从指定节点出发进行拓扑排序（Kahn算法）
     *
     * @param startNode 起始节点ID
     * @return 从起始节点开始的拓扑排序结果
     * @throws IllegalArgumentException 当起始节点有依赖时抛出
     * @throws CycleDetectedException 当检测到循环依赖时抛出
     */
    public List<String> topologicalSortFromNode(String startNode) {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        List<String> result = new ArrayList<>();

        adjacencyList.keySet().forEach(node -> inDegree.put(node, 0));
        adjacencyList.values().forEach(targets ->
                targets.forEach(target ->
                        inDegree.put(target, inDegree.getOrDefault(target, 0) + 1)
                )
        );

        if (inDegree.getOrDefault(startNode, -1) != 0) {
            throw new IllegalArgumentException("起始节点必须无依赖");
        }
        queue.add(startNode);

        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String neighbor : adjacencyList.get(node)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (result.size() != adjacencyList.size()) {
            throw new CycleDetectedException("流程存在循环依赖");
        }
        return result;
    }

}
