package cn.fxbin.bubble.flow.core.provider;

import cn.fxbin.bubble.core.dataobject.PageResult;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.util.ObjectUtils;
import cn.fxbin.bubble.data.mybatisplus.util.PageUtils;
import cn.fxbin.bubble.flow.core.builder.FlowExpressionBuilder;
import cn.fxbin.bubble.flow.core.enums.FlowPublishStatus;
import cn.fxbin.bubble.flow.core.enums.FlowType;
import cn.fxbin.bubble.flow.core.enums.PluginType;
import cn.fxbin.bubble.flow.core.exception.FlowNotFoundException;
import cn.fxbin.bubble.flow.core.mapper.*;
import cn.fxbin.bubble.flow.core.model.FlowChain;
import cn.fxbin.bubble.flow.core.model.dto.FlowDefinitionDTO;
import cn.fxbin.bubble.flow.core.model.dto.FlowDefinitionQueryDTO;
import cn.fxbin.bubble.flow.core.model.entity.*;
import cn.fxbin.bubble.plugin.satoken.util.TokenUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.SystemClock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FlowDefinitionProvider
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDefinitionProvider {

    private final FlowExpressionBuilder flowExpressionBuilder;

    private final FlowDefinitionMapper flowDefinitionMapper;

    private final FlowVersionHistoryMapper flowVersioHistoryMapper;

    private final FlowNodeMapper flowNodeMapper;

    private final FlowEdgeMapper flowEdgeMapper;

    private final FlowExecutionLogMapper flowExecutionLogMapper;

    private final FlowNodeExecutionLogMapper flowNodeExecutionLogMapper;

    /**
     * 保存流程定义
     *
     * 版本管理规则：
     * 1. 新建流程时，初始版本设置为1
     * 2. 更新流程时，保持当前版本不变
     * 3. 发布流程时，版本号 = 历史版本数量 + 1
     *
     * 保存逻辑：
     * 1. 创建或更新流程定义
     * 2. 处理节点（新增/更新/删除）
     * 3. 处理边（新增/更新/删除）
     *
     * @param dto 流程定义DTO，包含流程基本信息和流程图数据
     * @throws ServiceException 如果流程验证失败
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveFlow(FlowDefinitionDTO dto) {
        // 1. 获取或创建流程定义
        boolean isNew;
        FlowDefinition flowDefinition;
        if (ObjectUtils.isEmpty(dto.getFlowId())) {
            isNew = true;
            flowDefinition = new FlowDefinition();
        } else {
            flowDefinition = flowDefinitionMapper.selectById(dto.getFlowId());
            isNew = flowDefinition == null;
        }

        if (isNew) {
            flowDefinition = new FlowDefinition();
            flowDefinition.setId(dto.getFlowId());
        }

        //设置工作流类型
        flowDefinition.setType(ObjectUtils.isNotEmpty(dto.getType()) ? dto.getType() : FlowType.EXPLORE);

        // 1.2 构建与设置el表达式
        if (ObjectUtils.isNotEmpty(dto.getSchema())) {
            FlowChain flowChain = dto.getSchema();
            String expr = flowExpressionBuilder.buildExpression(flowChain.getNodes(), flowChain.getEdges());
            flowDefinition.setEl(expr);
        }

        // 2. 更新流程定义基本信息
        flowDefinition.setName(dto.getName());
        flowDefinition.setDescription(dto.getDescription());
        // 2.1 智能状态管理
        flowDefinition.setStatus(determineTargetStatus(flowDefinition, dto));
        if (isNew) {
            // 新建流程初始版本为1
            flowDefinition.setVersion(1);
        }

        // 3. 保存流程定义
        if (isNew) {
            flowDefinitionMapper.insert(flowDefinition);
        } else {
            // 3.1 更新当前版本
            flowDefinitionMapper.updateById(flowDefinition);
        }

        // 4. 处理节点
        if (dto.getSchema() != null) {
            // 4.1 获取现有节点ID列表
            List<String> existingNodeIds = flowNodeMapper.selectList(
                new LambdaQueryWrapper<FlowNode>()
                    .eq(FlowNode::getFlowId, flowDefinition.getId())
            ).stream().map(FlowNode::getId).collect(Collectors.toList());

            // 4.2 获取新节点ID列表
            List<String> newNodeIds = dto.getSchema().getNodes().stream()
                    .map(FlowNode::getId).toList();

            // 4.3 找出需要删除的节点
            existingNodeIds.removeAll(newNodeIds);
            if (!existingNodeIds.isEmpty()) {
                flowNodeMapper.deleteByIds(existingNodeIds);
            }

            // 4.4 更新或插入节点
            for (FlowNode node : dto.getSchema().getNodes()) {
                node.setFlowId(flowDefinition.getId());
                FlowNode existingNode = flowNodeMapper.selectById(node.getId());

                if (existingNode == null) {
                    flowNodeMapper.insert(node);
                } else {
                    flowNodeMapper.updateById(node);
                }
            }
        }

        // 5. 处理边
        if (dto.getSchema() != null) {
            // 5.1 获取现有边ID列表
            List<Long> existingEdgeIds = flowEdgeMapper.selectList(
                new LambdaQueryWrapper<FlowEdge>()
                    .eq(FlowEdge::getFlowId, flowDefinition.getId())
            ).stream().map(FlowEdge::getId).collect(Collectors.toList());

            // 5.2 获取新边ID列表
            List<Long> newEdgeIds = dto.getSchema().getEdges().stream()
                    .map(FlowEdge::getId).toList();

            // 5.3 找出需要删除的边
            existingEdgeIds.removeAll(newEdgeIds);
            if (!existingEdgeIds.isEmpty()) {
                flowEdgeMapper.deleteByIds(existingEdgeIds);
            }

            // 5.4 更新或插入边
            for (FlowEdge edge : dto.getSchema().getEdges()) {
                edge.setFlowId(flowDefinition.getId());
                FlowEdge existingEdge = flowEdgeMapper.selectById(edge.getId());

                if (existingEdge == null) {
                    flowEdgeMapper.insert(edge);
                } else {
                    flowEdgeMapper.updateById(edge);
                }
            }
        }
        return flowDefinition.getId();
    }

    /**
     * 确定目标状态
     * 规则：
     * 1. 新建流程：使用DTO中的状态（通常为DRAFT）
     * 2. 已发布流程被编辑：自动转为DRAFT状态
     * 3. 草稿流程：保持DTO中的状态
     */
    private FlowPublishStatus determineTargetStatus(FlowDefinition existingFlow, FlowDefinitionDTO dto) {
        // 新建流程，使用DTO状态
        if (existingFlow == null || existingFlow.getStatus() == null) {
            return dto.getStatus() != null ? dto.getStatus() : FlowPublishStatus.DRAFT;
        }

        // 如果当前是已发布状态，且有内容变更，自动转为草稿
        if (FlowPublishStatus.PUBLISHED.equals(existingFlow.getStatus())) {
            if (hasContentChanged(existingFlow, dto)) {
                log.info("Published flow {} has been modified, automatically converting to DRAFT status",
                        existingFlow.getId());
                return FlowPublishStatus.DRAFT;
            }
            // 没有内容变更，保持已发布状态
            return FlowPublishStatus.PUBLISHED;
        }

        // 草稿状态，使用DTO中的状态
        return dto.getStatus() != null ? dto.getStatus() : FlowPublishStatus.DRAFT;
    }

    /**
     * 检查内容是否发生变更
     */
    private boolean hasContentChanged(FlowDefinition existingFlow, FlowDefinitionDTO dto) {
        // 检查基本信息变更
        if (!Objects.equals(existingFlow.getName(), dto.getName()) ||
                !Objects.equals(existingFlow.getDescription(), dto.getDescription())) {
            return true;
        }

        // 检查流程图结构变更（通过EL表达式对比）
        if (dto.getSchema() != null) {
            String newExpr = flowExpressionBuilder.buildExpression(
                    dto.getSchema().getNodes(), dto.getSchema().getEdges());
            return !Objects.equals(existingFlow.getEl(), newExpr);
        }

        return false;
    }

    /**
     * 分页查询流程列表
     */
    public PageResult<FlowDefinition> listFlows(FlowDefinitionQueryDTO dto) {
        // 1. 构建查询条件
        LambdaQueryWrapper<FlowDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeIfPresent(FlowDefinition::getName, dto.getName());

        // 2. 分页查询
        PageResult<FlowDefinition> pageResult = flowDefinitionMapper.selectPage(dto, queryWrapper);

        // 3. 转换为PageResult
        return PageUtils.buildPageResult(dto, pageResult.getList(), pageResult.getTotal());
    }

    /**
     * 获取流程当前版本号
     *
     * @param flowId 流程ID
     * @return 当前版本号
     * @throws FlowNotFoundException 如果指定ID的流程不存在
     */
    public Integer getCurrentVersion(Long flowId) {
        // 1. 获取流程定义基本信息
        FlowDefinition flowDefinition = flowDefinitionMapper.selectById(flowId);
        if (flowDefinition == null) {
            throw new FlowNotFoundException("Flow not found with id: " + flowId);
        }
        return flowDefinition.getVersion();
    }

    /**
     * 获取流程详情（包含节点和边）
     * 返回内容包括：
     * 1. 流程基本信息（ID、名称、描述、状态、版本等）
     * 2. 流程节点列表（包含所有节点属性）
     * 3. 流程边列表（包含所有边属性）
     * 
     * @param flowId 流程ID
     * @return 完整的流程定义DTO对象
     * @throws FlowNotFoundException 如果指定ID的流程不存在
     */
    public FlowDefinitionDTO getFlowDetail(Long flowId) {
        // 1. 获取流程定义基本信息
        FlowDefinition flowDefinition = flowDefinitionMapper.selectById(flowId);
        if (flowDefinition == null) {
            throw new FlowNotFoundException("Flow not found with id: " + flowId);
        }
        
        // 2. 转换为DTO
        FlowDefinitionDTO dto = new FlowDefinitionDTO();
        dto.setFlowId(flowDefinition.getId());
        dto.setName(flowDefinition.getName());
        dto.setDescription(flowDefinition.getDescription());
        dto.setStatus(flowDefinition.getStatus());
        dto.setVersion(flowDefinition.getVersion());
        dto.setEl(flowDefinition.getEl());
        
        // 3. 获取流程相关的所有节点
        List<FlowNode> nodes = flowNodeMapper.selectList(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, flowId)
        );
        
        // 4. 获取流程相关的所有边
        List<FlowEdge> edges = flowEdgeMapper.selectList(
            new LambdaQueryWrapper<FlowEdge>()
                .eq(FlowEdge::getFlowId, flowId)
        );
        
        // 5. 设置流程图数据
        FlowChain schema = new FlowChain();
        schema.setNodes(nodes);
        schema.setEdges(edges);
        dto.setSchema(schema);
        
        return dto;
    }

    /**
     * 删除流程定义
     * 删除规则：
     * 1. 已发布的流程不能删除
     * 2. 删除流程时会级联删除所有关联的节点和边
     * 
     * @param flowId 流程ID
     * @throws FlowNotFoundException 如果流程不存在
     * @throws ServiceException 如果流程已发布
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlow(Long flowId) {
        // 1. 检查流程是否存在
        FlowDefinition flowDefinition = flowDefinitionMapper.selectById(flowId);
        if (flowDefinition == null) {
            throw new FlowNotFoundException("Flow not found with id: " + flowId);
        }
        
        // 2. 检查流程状态，已发布的流程不能删除
        if (FlowPublishStatus.PUBLISHED.equals(flowDefinition.getStatus())) {
            throw new IllegalStateException("Cannot delete published flow: " + flowId);
        }
        
        // 3. 删除相关的节点
        flowNodeMapper.delete(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, flowId)
        );
        
        // 4. 删除相关的边
        flowEdgeMapper.delete(
            new LambdaQueryWrapper<FlowEdge>()
                .eq(FlowEdge::getFlowId, flowId)
        );
        
        // 5. 删除流程定义
        flowDefinitionMapper.deleteById(flowId);

        // 6. 删除历史版本记录
        flowVersioHistoryMapper.delete(
            new LambdaQueryWrapper<FlowVersionHistory>()
               .eq(FlowVersionHistory::getFlowId, flowId));

        // 7. 删除日志记录
        // 7.1 获取流程日志
        List<FlowExecutionLog> executionLogList = flowExecutionLogMapper.selectList(FlowExecutionLog::getFlowId, flowId);
        flowExecutionLogMapper.delete(
            new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
        );

        // 7.2 删除节点日志
        // TODO 这里可能会存在性能问题；数据量大的情况下，需要分批次删除
        List<Long> executionLogIdList = executionLogList.stream().map(FlowExecutionLog::getId).toList();
        flowNodeExecutionLogMapper.delete(
            new LambdaQueryWrapper<FlowNodeExecutionLog>()
               .in(FlowNodeExecutionLog::getFlowExecutionLogId, executionLogIdList)
        );

        log.info("Successfully deleted flow with id: {}", flowId);
    }

    /**
     * 发布流程
     * 
     * 业务规则：
     * 1. 流程必须包含至少一个开始节点和一个结束节点
     * 2. 流程必须包含至少一条边
     * 3. 已发布的流程不能重复发布
     * 
     * 版本管理：
     * 1. 构建完整快照（节点+边+配置）
     * 2. 保存当前版本到历史记录
     * 3. 新版本号 = 历史版本数量 + 1
     * 4. 更新流程状态为已发布
     * 
     * @param flowId 流程ID
     * @throws FlowNotFoundException 如果流程不存在
     * @throws ServiceException 如果流程不符合发布条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishFlow(Long flowId) {
        // 1. 检查流程是否存在
        FlowDefinition flowDefinition = flowDefinitionMapper.selectById(flowId);
        if (flowDefinition == null) {
            throw new FlowNotFoundException("Flow not found with id: " + flowId);
        }
        
        // 2. 验证流程是否可以发布
        validateFlowForPublish(flowDefinition);

        // 3. 构建完整快照
        FlowChain completeSnapshot = buildCompleteSnapshot(flowDefinition.getId());
        
        // 4. 计算新版本号（根据历史版本数量）
        Long historyCount = flowVersioHistoryMapper.selectCount(
            new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowDefinition.getId())
        );
        Integer newVersion = (int) (historyCount + 1);
        
        // 5. 保存历史版本（包含完整快照）
        FlowVersionHistory history = new FlowVersionHistory();
        history.setFlowId(flowDefinition.getId());
        history.setVersion(newVersion);
        history.setName(flowDefinition.getName());
        history.setDescription(flowDefinition.getDescription());
        history.setActive(true);
        history.setSnapshot(completeSnapshot);
        history.setCreateBy(TokenUtils.getCurrentUserId());
        history.setUpdateBy(TokenUtils.getCurrentUserId());
        flowVersioHistoryMapper.insert(history);
        
        // 6. 更新流程状态为已发布
        flowDefinition.setStatus(FlowPublishStatus.PUBLISHED);
        flowDefinition.setVersion(newVersion);
        flowDefinitionMapper.updateById(flowDefinition);
        
        log.info("Successfully published flow {} with version {}", flowId, newVersion);
    }

    /**
     * 从历史版本克隆创建新流程
     * 
     * 克隆规则：
     * 1. 创建新的流程定义，初始版本为1
     * 2. 复制历史版本的所有节点和边
     * 3. 新流程状态为草稿
     * 4. 维护节点ID映射关系，更新边和配置中的节点引用
     * 
     * @param historyId 历史版本ID
     * @return 新创建的流程ID
     * @throws ServiceException 如果历史版本不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public Long cloneFromHistory(Long historyId) {
        log.info("Start cloning flow from history version: {}", historyId);
        
        // 1. 查询历史版本快照
        FlowVersionHistory history = flowVersioHistoryMapper.selectById(historyId);
        if (history == null) {
            throw new ServiceException("History version not found: " + historyId);
        }

        // 2. 创建新流程定义
        FlowDefinition newFlow = new FlowDefinition();
        newFlow.setName(history.getName() + "_clone_" + SystemClock.now());
        newFlow.setDescription(history.getDescription());
        newFlow.setStatus(FlowPublishStatus.DRAFT);
        // 初始版本为1
        newFlow.setVersion(1);
        newFlow.setCreateBy(TokenUtils.getCurrentUserId());
        newFlow.setUpdateBy(TokenUtils.getCurrentUserId());
        flowDefinitionMapper.insert(newFlow);
        
        log.info("Created new flow definition with ID: {}", newFlow.getId());

        // 3. 生成初始版本记录
        FlowVersionHistory initialVersion = new FlowVersionHistory();
        initialVersion.setFlowId(newFlow.getId());
        initialVersion.setVersion(1);
        initialVersion.setName(newFlow.getName());
        initialVersion.setDescription(newFlow.getDescription());
        flowVersioHistoryMapper.insert(initialVersion);
        
        log.info("Created initial version record for new flow");

        // 4. 复制历史版本的节点和边
        FlowChain snapshot = history.getSnapshot();
        Long newFlowId = newFlow.getId();
        Map<String, String> nodeIdMapping = new HashMap<>();


        try {
            // 4.1 处理节点数据
            List<FlowNode> nodes = snapshot.getNodes();
            if (ObjectUtils.isNotEmpty(nodes)) {
                for (FlowNode oldNode : nodes) {
                    FlowNode newNode = new FlowNode();
                    BeanUtil.copyProperties(oldNode, newNode, "id", "flowId");
                    
                    // 设置新节点属性
                    newNode.setFlowId(newFlowId);
                    
                    // 插入新节点
                    flowNodeMapper.insert(newNode);
                    
                    // 记录节点ID映射
                    nodeIdMapping.put(oldNode.getId(), newNode.getId());
                    log.debug("Copied node from {} to {}", oldNode.getId(), newNode.getId());
                }
            }

            // 4.2 处理边数据
            List<FlowEdge> edges = snapshot.getEdges();
            if (ObjectUtils.isNotEmpty(edges)) {
                for (FlowEdge oldEdge : edges) {
                    FlowEdge newEdge = new FlowEdge();
                    BeanUtil.copyProperties(oldEdge, newEdge, "id", "flowId", "sourceNodeId", "targetNodeId");
                    
                    // 设置新边属性
                    newEdge.setFlowId(newFlowId);
                    newEdge.setSourceNodeId(nodeIdMapping.get(oldEdge.getSourceNodeId()));
                    newEdge.setTargetNodeId(nodeIdMapping.get(oldEdge.getTargetNodeId()));
                    
                    flowEdgeMapper.insert(newEdge);
                    log.debug("Copied edge from {} to new flow", oldEdge.getId());
                }
            }

            flowVersioHistoryMapper.insert(initialVersion);
        } catch (Exception e) {
            log.error("Failed to clone flow from history version: {}", historyId, e);
            throw new ServiceException("Failed to clone flow from history version: " + e.getMessage(), e);
        }

        log.info("Successfully cloned flow from history version {} to new flow {}", historyId, newFlowId);
        return newFlowId;
    }

    /**
     * 版本重放 - 从历史版本重建流程定义
     * <p>根据历史版本快照重建完整的流程定义，包括节点和边</p>
     * 
     * @param flowId 流程ID
     * @param version 目标版本号
     * @return 重建的流程定义DTO
     * @throws ServiceException 如果历史版本不存在
     */
    public FlowDefinitionDTO replayFlowVersion(Long flowId, Integer version) {
        log.info("Replaying flow {} to version {}", flowId, version);
        
        // 1. 查询历史版本快照
        FlowVersionHistory history = flowVersioHistoryMapper.selectOne(
            new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .eq(FlowVersionHistory::getVersion, version)
        );
        
        if (history == null) {
            throw new ServiceException(String.format("History version not found: flowId=%d, version=%d", flowId, version));
        }
        
        // 2. 从快照重建流程定义DTO
        FlowDefinitionDTO dto = new FlowDefinitionDTO();
        dto.setFlowId(history.getFlowId());
        dto.setName(history.getName());
        dto.setDescription(history.getDescription());
        dto.setVersion(history.getVersion());
        // 历史版本都是已发布状态
        dto.setStatus(FlowPublishStatus.PUBLISHED);
        
        // 3. 设置快照数据
        if (history.getSnapshot() != null) {
            dto.setSchema(history.getSnapshot());
            
            // 4. 重建EL表达式
            FlowChain snapshot = history.getSnapshot();
            if (snapshot.getNodes() != null && snapshot.getEdges() != null) {
                String expr = flowExpressionBuilder.buildExpression(snapshot.getNodes(), snapshot.getEdges());
                dto.setEl(expr);
            }
        }
        
        log.info("Successfully replayed flow {} to version {}", flowId, version);
        return dto;
    }

    /**
     * 构建完整的流程快照
     * <p>包含流程的所有节点、边和配置信息，用于版本历史存储</p>
     * 
     * @param flowId 流程ID
     * @return 完整的流程快照
     */
    private FlowChain buildCompleteSnapshot(Long flowId) {
        // 1. 获取所有节点
        List<FlowNode> nodes = flowNodeMapper.selectList(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, flowId)
        );
        
        // 2. 获取所有边
        List<FlowEdge> edges = flowEdgeMapper.selectList(
            new LambdaQueryWrapper<FlowEdge>()
                .eq(FlowEdge::getFlowId, flowId)
        );
        
        // 3. 构建快照对象
        FlowChain snapshot = new FlowChain();
        snapshot.setNodes(nodes);
        snapshot.setEdges(edges);
        
        log.debug("Built complete snapshot for flow {}: {} nodes, {} edges", 
                 flowId, nodes.size(), edges.size());
        
        return snapshot;
    }

    /**
     * 验证流程是否可以发布
     *
     * 规则：
     * 1. 流程必须包含至少一个开始节点和一个结束节点
     * 2. 流程必须包含至少一条边
     * 3. 流程不能已经是发布状态
     *
     * @param flowDefinition 流程定义对象
     * @throws ServiceException 如果流程不符合发布条件
     */
    private void validateFlowForPublish(FlowDefinition flowDefinition) {
        // 允许重复发布；移除校验
        // 1. 检查是否已经是发布状态;
//        if (FlowPublishStatus.PUBLISHED.equals(flowDefinition.getStatus())) {
//            throw new ServiceException("Flow is already published: " + flowDefinition.getId());
//        }
        
        // 2. 检查是否有节点
        long nodeCount = flowNodeMapper.selectCount(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, flowDefinition.getId())
        );
        if (nodeCount == 0) {
            throw new ServiceException("Flow has no nodes: " + flowDefinition.getId());
        }
        
        // 3. 检查是否有边
        long edgeCount = flowEdgeMapper.selectCount(
            new LambdaQueryWrapper<FlowEdge>()
                .eq(FlowEdge::getFlowId, flowDefinition.getId())
        );
        if (edgeCount == 0) {
            throw new ServiceException("Flow has no edges: " + flowDefinition.getId());
        }
        
        // 4. 检查是否有开始节点和结束节点
        long startNodeCount = flowNodeMapper.selectCount(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, flowDefinition.getId())
                .eq(FlowNode::getNodeType, PluginType.START_NODE)
        );
        if (startNodeCount == 0) {
            throw new ServiceException("Flow has no start node: " + flowDefinition.getId());
        }


        // 注释，暂时不支持结束节点
//        long endNodeCount = flowNodeMapper.selectCount(
//            new LambdaQueryWrapper<FlowNode>()
//                .eq(FlowNode::getFlowId, flowDefinition.getId())
//                .eq(FlowNode::getNodeType, PluginType.END_NODE)
//        );
//        if (endNodeCount == 0) {
//            throw new ServiceException("Flow has no end node: " + flowDefinition.getId());
//        }
    }

    


}
