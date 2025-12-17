package cn.fxbin.bubble.flow.core.provider;

import cn.fxbin.bubble.core.util.CollectionUtils;
import cn.fxbin.bubble.core.util.ObjectUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.fxbin.bubble.core.dataobject.PageRequest;
import cn.fxbin.bubble.core.dataobject.PageResult;
import cn.fxbin.bubble.flow.core.cache.FlowNodeCache;
import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import cn.fxbin.bubble.flow.core.mapper.FlowDefinitionMapper;
import cn.fxbin.bubble.flow.core.mapper.FlowExecutionLogMapper;
import cn.fxbin.bubble.flow.core.mapper.FlowNodeExecutionLogMapper;
import cn.fxbin.bubble.flow.core.model.dto.FlowExecutionProgressDTO;
import cn.fxbin.bubble.flow.core.model.dto.FlowExecutionRecordQueryDTO;
import cn.fxbin.bubble.flow.core.model.dto.NodeExecutionProgressDTO;
import cn.fxbin.bubble.flow.core.model.entity.FlowDefinition;
import cn.fxbin.bubble.flow.core.model.entity.FlowExecutionLog;
import cn.fxbin.bubble.flow.core.model.entity.FlowNode;
import cn.fxbin.bubble.flow.core.model.entity.FlowNodeExecutionLog;
import cn.fxbin.bubble.flow.core.util.FlowUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FlowExecutorLogProvider
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/5/9 11:11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExecutionLogProvider {

    private final FlowExecutionLogMapper flowExecutionLogMapper;

    private final FlowNodeExecutionLogMapper flowNodeExecutionLogMapper;

    private final FlowDefinitionMapper flowDefinitionMapper;

    private final List<String> sortedFields = Lists.newArrayList("create_time", "update_time");

    /**
     * 保存或更新工作流执行日志
     * <p>自动关联当前流程版本，确保执行记录与版本的关联性</p>
     *
     * @param flowExecutionLog 工作流执行日志实体
     * @author fxbin
     */
    public void saveOrUpdateFlowExecutionLog(FlowExecutionLog flowExecutionLog) {
        // 如果是新记录且未设置版本，则自动关联当前流程版本
        if (flowExecutionLog.getId() == null && flowExecutionLog.getFlowVersion() == null) {
            Integer currentVersion = getCurrentFlowVersion(flowExecutionLog.getFlowId());
            if (currentVersion != null) {
                flowExecutionLog.setFlowVersion(currentVersion);
                log.debug("Auto-associated execution log with flow version: flowId={}, version={}", 
                         flowExecutionLog.getFlowId(), currentVersion);
            }
        }
        
        if (flowExecutionLog.getId() == null) {
            flowExecutionLogMapper.insert(flowExecutionLog);
        } else {
            flowExecutionLogMapper.updateById(flowExecutionLog);
        }
    }

    /**
     * 保存带版本关联的工作流执行日志
     * <p>明确指定版本号，用于历史版本执行场景</p>
     *
     * @param flowExecutionLog 工作流执行日志实体
     * @param flowVersion 流程版本号
     * @author fxbin
     */
    public void saveFlowExecutionLogWithVersion(FlowExecutionLog flowExecutionLog, Integer flowVersion) {
        flowExecutionLog.setFlowVersion(flowVersion);
        this.saveOrUpdateFlowExecutionLog(flowExecutionLog);
        log.info("Saved execution log with explicit version: flowId={}, version={}, executionId={}", 
                flowExecutionLog.getFlowId(), flowVersion, flowExecutionLog.getFlowInstanceId());
    }

    /**
     * 保存节点执行日志
     *
     * @param nodeExecutionLog 节点执行日志实体
     * @author fxbin
     */
    public void saveOrUpdateNodeExecutionLog(FlowNodeExecutionLog nodeExecutionLog) {
        if (nodeExecutionLog.getId() == null) {
            flowNodeExecutionLogMapper.insert(nodeExecutionLog);
        } else {
            flowNodeExecutionLogMapper.updateById(nodeExecutionLog);
        }
    }

    /**
     * 根据执行ID或流程ID查询流程执行进度
     * <p>
     * 如果提供了 executionId，则按执行ID查询。
     * 如果只提供了 flowId，则查询该流程最新一次的执行进度。
     * 如果流程正在执行，则会包含定义中存在但尚未执行的节点，其状态为 PENDING。
     * </p>
     *
     * @param executionId 执行ID (可选)
     * @param flowId      流程ID (如果executionId为空，则此项必填)
     * @return {@see FlowExecutionProgressDTO} 流程执行进度详情，如果找不到则返回null
     * @author fxbin
     */
    public FlowExecutionProgressDTO getFlowExecutionProgress(String executionId, Long flowId) {
        FlowExecutionLog flowLog;
        if (executionId != null) {
            flowLog = flowExecutionLogMapper.selectOne(
                    Wrappers.<FlowExecutionLog>lambdaQuery().
                            eq(FlowExecutionLog::getFlowInstanceId, executionId)
            );
        } else if (flowId != null) {
            // 查询最新的执行记录
            flowLog = flowExecutionLogMapper.selectOne(
                    Wrappers.<FlowExecutionLog>lambdaQuery()
                            .eq(FlowExecutionLog::getFlowId, flowId)
                            .orderByDesc(FlowExecutionLog::getStartTime)
                            .last("LIMIT 1")
            );
        } else {
            // 两个ID都为空，无法查询
            return null;
        }

        if (flowLog == null) {
            return null;
        }

        List<FlowNodeExecutionLog> executedNodeLogs = flowNodeExecutionLogMapper.selectList(
                Wrappers.<FlowNodeExecutionLog>lambdaQuery()
                        .eq(FlowNodeExecutionLog::getFlowExecutionLogId, flowLog.getId())
                        .orderByAsc(FlowNodeExecutionLog::getStartTime)
        );

        List<NodeExecutionProgressDTO> nodeProgressList = executedNodeLogs.stream()
                .map(nodeLog -> NodeExecutionProgressDTO.builder()
                        .nodeExecutionLogId(nodeLog.getId())
                        .nodeId(nodeLog.getNodeId())
                        .nodeName(nodeLog.getNodeName())
                        .nodeType(nodeLog.getNodeType())
                        .status(nodeLog.getStatus())
                        .startTime(nodeLog.getStartTime())
                        .endTime(nodeLog.getEndTime())
                        .durationMs(nodeLog.getDurationMs())
                        .errorMessage(nodeLog.getErrorMessage())
                        .build())
                .collect(Collectors.toList());

        // 如果流程正在运行，则补充尚未执行的节点信息
        if (FlowExecStatus.RUNNING == flowLog.getStatus() && flowLog.getEndTime() == null) {
            List<FlowNode> allNodes = FlowNodeCache.getAllNodes(flowLog.getFlowId());
            if (allNodes != null && !allNodes.isEmpty()) {
                Set<String> executedNodeIds = executedNodeLogs.stream()
                        .map(FlowNodeExecutionLog::getNodeId)
                        .collect(Collectors.toSet());

                List<NodeExecutionProgressDTO> pendingNodes = new ArrayList<>();
                for (FlowNode node : allNodes) {
                    if (!executedNodeIds.contains(node.getId())) {
                        pendingNodes.add(NodeExecutionProgressDTO.builder()
                                .nodeId(node.getId())
                                .nodeName(node.getName())
                                .nodeType(node.getNodeType().name())
                                .status(FlowExecStatus.PENDING)
                                .build());
                    }
                }
                // 将pending的节点也加入到列表中，可以考虑排序，但当前按执行日志排序，pending的自然在后面或前面
                nodeProgressList.addAll(pendingNodes);
            }
        }

        return FlowExecutionProgressDTO.builder()
                .flowExecutionLogId(flowLog.getId())
                .flowId(FlowUtils.getFlowIdExtractChainId(flowLog.getFlowInstanceId()))
                .flowName(flowLog.getFlowDefinitionId())
                .status(flowLog.getStatus())
                .startTime(flowLog.getStartTime())
                .endTime(flowLog.getEndTime())
                .durationMs(flowLog.getDurationMs())
                .errorMessage(flowLog.getErrorMessage())
                .nodeProgressList(nodeProgressList)
                .build();
    }



    /**
     * 查询流程执行记录
     *
     * @param dto 查询参数
     * @return 流程执行记录列表
     * @author fxbin
     */
    public PageResult<FlowExecutionLog> queryFlowExecutionRecords(FlowExecutionRecordQueryDTO dto) {
        LambdaQueryWrapper<FlowExecutionLog> queryWrapper = Wrappers.<FlowExecutionLog>lambdaQuery()
                .inIfPresent(FlowExecutionLog::getFlowId, dto.getFlowIds())
                .eqIfPresent(FlowExecutionLog::getFlowVersion, dto.getVersion())
                .eqIfPresent(FlowExecutionLog::getStatus, dto.getExecStatus())
                .eqIfPresent(FlowExecutionLog::getTriggerType, dto.getCallType())
                .orderByDesc(FlowExecutionLog::getStartTime);

        if (ObjectUtils.isNotEmpty(dto.getStartTime()) && ObjectUtils.isNotEmpty(dto.getEndTime())) {
            queryWrapper.between(FlowExecutionLog::getStartTime, dto.getStartTime(), dto.getEndTime());
        }

        // 排序字段
        List<PageRequest.SortItem> sorts = dto.getSorts();
        if (CollectionUtils.isNotEmpty(sorts)) {
            sorts.forEach(sort -> {
                String field = sort.getField();
                if (sortedFields.contains(field)) {

                    if ("create_time".equals(field)) {
                        if (sort.isAsc()) {
                            queryWrapper.orderByAsc(FlowExecutionLog::getCreateTime);
                        } else {
                            queryWrapper.orderByDesc(FlowExecutionLog::getCreateTime);
                        }
                    }

                    if ("update_time".equals(field)) {
                        if (sort.isAsc()) {
                            queryWrapper.orderByAsc(FlowExecutionLog::getUpdateTime);
                        } else {
                            queryWrapper.orderByDesc(FlowExecutionLog::getUpdateTime);
                        }
                    }
                }
            });
        }


        return flowExecutionLogMapper.selectPage(dto, queryWrapper);
    }

    /**
     * 根据流程执行记录ID和节点ID获取节点执行记录
     *
     * @param flowExecutionLogId 流程执行记录ID
     * @param nodeId             节点ID
     * @return 节点执行记录
     */
    public FlowNodeExecutionLog getNodeExecutionLogByLogIdAndNodeId(Long flowExecutionLogId, String nodeId) {
        LambdaQueryWrapper<FlowNodeExecutionLog> queryWrapper = new LambdaQueryWrapper<FlowNodeExecutionLog>()
                .eq(FlowNodeExecutionLog::getFlowExecutionLogId, flowExecutionLogId)
                .eq(FlowNodeExecutionLog::getNodeId, nodeId);

        return flowNodeExecutionLogMapper.selectOne(queryWrapper);
    }

    /**
     * 获取流程当前版本号
     * <p>用于自动关联执行记录与版本</p>
     *
     * @param flowId 流程ID
     * @return 当前版本号，如果流程不存在则返回null
     */
    private Integer getCurrentFlowVersion(Long flowId) {
        try {
            // 查询流程定义获取当前版本
            FlowDefinition flowDefinition = flowDefinitionMapper.selectById(flowId);
            return flowDefinition != null ? flowDefinition.getVersion() : null;
        } catch (Exception e) {
            log.warn("Failed to get current flow version for flowId: {}", flowId, e);
            return null;
        }
    }

    /**
     * 统计版本执行记录数量
     * <p>用于版本管理和监控</p>
     *
     * @param flowId 流程ID
     * @param version 版本号
     * @return 执行记录数量
     */
    public Long countExecutionLogsByVersion(Long flowId, Integer version) {
        LambdaQueryWrapper<FlowExecutionLog> queryWrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
                .eq(FlowExecutionLog::getFlowVersion, version);
        
        Long count = flowExecutionLogMapper.selectCount(queryWrapper);
        log.debug("Counted {} execution logs for flowId={}, version={}", count, flowId, version);
        return count;
    }
}
