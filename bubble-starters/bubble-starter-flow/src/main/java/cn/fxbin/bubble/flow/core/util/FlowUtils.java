package cn.fxbin.bubble.flow.core.util;

import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.flow.core.consts.RuleFlowConst;
import cn.fxbin.bubble.flow.core.context.FlowContextHolder;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import cn.fxbin.bubble.flow.core.model.entity.FlowEdge;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.hutool.core.date.SystemClock;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FlowUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/5/7 14:38
 */

public class FlowUtils {

    /**
     * 从 chainId 中提取出 flowId
     *
     * @param chainId chainId
     * @return flowId
     */
    public static Long getFlowIdExtractChainId(String chainId) {
        if (chainId == null || chainId.isEmpty()) {
            throw new ServiceException("chainId 不能为空");
        }

        int firstUnderscoreIndex = chainId.indexOf('_');
        if (firstUnderscoreIndex == -1 || firstUnderscoreIndex == chainId.length() - 1) {
            throw new ServiceException("chainId 必须包含下划线且下划线不能位于末尾");
        }

        int lastUnderscoreIndex = chainId.lastIndexOf('_');
        String idPart = chainId.substring(firstUnderscoreIndex + 1, lastUnderscoreIndex);
        if (idPart.isEmpty()) {
            throw new ServiceException("chainId 中下划线之间的部分不能为空");
        }
        try {
            return Long.valueOf(idPart);
        } catch (NumberFormatException e) {
            throw new ServiceException("chainId 下划线后的部分必须为有效数字: " + idPart, e);
        }
    }

    /**
     * 生成 executionId
     *
     * @param flowId flowId
     * @return executionId
     */
    public static String generateExecutionId(Long flowId) {
        return StringUtils.format(RuleFlowConst.RUN_CHAIN_ID, flowId, SystemClock.now());
    }

    /**
     * 获取流程上下文
     * @param chainId chainId
     * @return FlowContextHolder
     */
    public static FlowContextHolder getContext(String chainId) {
        Long flowId = FlowUtils.getFlowIdExtractChainId(chainId);
        // 获取流程上下文
        return FlowContextHolder.loadState(flowId, chainId);
    }

    /**
     * 是试运行
     *
     * @param chainId 链id
     * @return boolean
     */
    public static boolean isTestRun(String chainId) {
        return chainId.startsWith("test_run");
    }

    /**
     * 不是试运行
     *
     * @param chainId 链id
     * @return boolean
     */
    public static boolean isNotTestRun(String chainId) {
        return !isTestRun(chainId);
    }

    public static void buildNodeRelation(FlowChain flowChain) {
        List<FlowNode> nodes = flowChain.getNodes();
        List<FlowEdge> edges = flowChain.getEdges();

        Map<String, FlowNode> nodeMap = nodes.stream().collect(Collectors.toMap(FlowNode::getId, Function.identity()));

        List<FlowEdge> allEdges = Lists.newArrayList();
        allEdges.addAll(edges);

        // 构建节点关系
        allEdges.forEach(edge -> {
            FlowNode sourceNode = nodeMap.get(edge.getSourceNodeId());
            FlowNode targetNode = nodeMap.get(edge.getTargetNodeId());

            sourceNode.addNextNode(targetNode);
            targetNode.addPreNode(sourceNode);
        });
    }



}
