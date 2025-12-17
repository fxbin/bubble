package cn.fxbin.bubble.flow.core.provider;

import cn.hutool.core.date.SystemClock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import cn.fxbin.bubble.flow.core.mapper.FlowArchiveMapper;
import cn.fxbin.bubble.flow.core.mapper.FlowArchiveOperationLogMapper;
import cn.fxbin.bubble.flow.core.mapper.FlowExecutionLogMapper;
import cn.fxbin.bubble.flow.core.mapper.FlowVersionHistoryMapper;
import cn.fxbin.bubble.flow.core.model.dto.FlowArchiveConfigDTO;
import cn.fxbin.bubble.flow.core.model.dto.FlowArchiveResultDTO;
import cn.fxbin.bubble.flow.core.model.entity.FlowArchiveOperationLog;
import cn.fxbin.bubble.flow.core.model.entity.FlowArchiveRecord;
import cn.fxbin.bubble.flow.core.model.entity.FlowExecutionLog;
import cn.fxbin.bubble.flow.core.model.entity.FlowVersionHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程版本归档服务
 * <p>提供版本归档、清理、备份等功能，支持版本生命周期管理</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowVersionArchiveProvider {

    private final FlowExecutionLogMapper flowExecutionLogMapper;
    private final FlowVersionHistoryMapper flowVersionHistoryMapper;
    private final FlowArchiveMapper flowArchiveMapper;
    private final FlowArchiveOperationLogMapper flowArchiveOperationLogMapper;

    /**
     * 执行版本归档
     * <p>根据指定策略归档流程版本</p>
     *
     * @param flowId 流程ID
     * @param config 归档配置
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public FlowArchiveResultDTO archiveVersions(Long flowId, FlowArchiveConfigDTO config) {
        log.info("Starting version archive for flow: {}, strategy: {}", flowId, config.getStrategy());
        
        FlowArchiveResultDTO result = new FlowArchiveResultDTO(flowId, config.getStrategy());
        
        try {
            // 1. 获取候选归档版本
            List<Integer> candidateVersions = getCandidateVersionsForArchive(flowId, config);
            result.setCandidateVersions(candidateVersions);
            
            if (candidateVersions.isEmpty()) {
                result.setSuccess(true);
                result.setMessage("没有符合归档条件的版本");
                log.info("No versions to archive for flow: {}", flowId);
                return result;
            }
            
            // 2. 验证归档安全性
            validateArchiveSafety(flowId, candidateVersions, result);
            
            // 3. 试运行模式
            if (config.isDryRun()) {
                result.setSuccess(true);
                result.setMessage("试运行完成，共" + candidateVersions.size() + "个版本符合归档条件");
                log.info("Dry run completed for flow: {}, {} versions would be archived", flowId, candidateVersions.size());
                return result;
            }
            
            // 4. 执行归档前备份
            if (config.isBackupBeforeArchive()) {
                performBackupBeforeArchive(flowId, candidateVersions, result);
            }
            
            // 5. 执行实际归档
            performActualArchive(flowId, candidateVersions, result);
            
            // 6. 生成统计信息
            generateArchiveStatistics(flowId, result);
            
            result.setSuccess(true);
            result.setMessage("归档完成，共归档" + result.getArchivedVersions().size() + "个版本");
            
            log.info("Version archive completed for flow: {}, archived: {} versions", 
                    flowId, result.getArchivedVersions().size());
            
        } catch (Exception e) {
            log.error("Version archive failed for flow: {}", flowId, e);
            result.setSuccess(false);
            result.setMessage("归档失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取候选归档版本
     */
    private List<Integer> getCandidateVersionsForArchive(Long flowId, FlowArchiveConfigDTO config) {
        List<Integer> candidates = switch (config.getStrategy()) {
            case BY_TIME -> getCandidatesByTime(flowId, config.getRetentionDays());
            case BY_COUNT -> getCandidatesByCount(flowId, config.getRetentionCount());
            case BY_USAGE -> getCandidatesByUsage(flowId, config.getMinUsageCount());
            case MANUAL -> config.getManualVersions() != null ?
                    new ArrayList<>(config.getManualVersions()) : new ArrayList<>();
        };

        // 排除当前正在使用的版本
        return filterActiveVersions(flowId, candidates);
    }

    /**
     * 按时间获取候选版本
     */
    private List<Integer> getCandidatesByTime(Long flowId, Integer retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        
        LambdaQueryWrapper<FlowVersionHistory> wrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .lt(FlowVersionHistory::getCreateTime, cutoffTime)
                .orderByAsc(FlowVersionHistory::getVersion);
        
        return flowVersionHistoryMapper.selectList(wrapper).stream()
                .map(FlowVersionHistory::getVersion)
                .collect(Collectors.toList());
    }

    /**
     * 按数量获取候选版本
     */
    private List<Integer> getCandidatesByCount(Long flowId, Integer retentionCount) {
        LambdaQueryWrapper<FlowVersionHistory> wrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .orderByDesc(FlowVersionHistory::getVersion);
        
        List<FlowVersionHistory> allVersions = flowVersionHistoryMapper.selectList(wrapper);

        // 版本数不超过保留数量，无需归档
        if (allVersions.size() <= retentionCount) {
            return new ArrayList<>();
        }
        
        // 返回需要归档的版本（保留最新的retentionCount个版本）
        return allVersions.stream()
                .skip(retentionCount)
                .map(FlowVersionHistory::getVersion)
                .collect(Collectors.toList());
    }

    /**
     * 按使用频率获取候选版本
     */
    private List<Integer> getCandidatesByUsage(Long flowId, Integer minUsageCount) {
        // 获取各版本的使用统计
        List<Map<String, Object>> usageStats = flowExecutionLogMapper.selectVersionExecutionStats(flowId);
        
        return usageStats.stream()
                .filter(stat -> {
                    Number totalCount = (Number) stat.get("totalCount");
                    return totalCount != null && totalCount.intValue() < minUsageCount;
                })
                .map(stat -> (Integer) stat.get("version"))
                .collect(Collectors.toList());
    }

    /**
     * 过滤掉正在活跃使用的版本
     */
    private List<Integer> filterActiveVersions(Long flowId, List<Integer> candidates) {
        // 获取最近7天内有执行记录的版本
        LocalDateTime recentTime = LocalDateTime.now().minusDays(7);
        
        LambdaQueryWrapper<FlowExecutionLog> wrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
                .ge(FlowExecutionLog::getStartTime, recentTime)
                .isNotNull(FlowExecutionLog::getFlowVersion);
        
        Set<Integer> activeVersions = flowExecutionLogMapper.selectList(wrapper).stream()
                .map(FlowExecutionLog::getFlowVersion)
                .collect(Collectors.toSet());
        
        return candidates.stream()
                .filter(version -> !activeVersions.contains(version))
                .collect(Collectors.toList());
    }

    /**
     * 验证归档安全性
     */
    private void validateArchiveSafety(Long flowId, List<Integer> candidateVersions, FlowArchiveResultDTO result) {
        // 1. 检查是否会归档所有版本
        LambdaQueryWrapper<FlowVersionHistory> wrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId);
        long totalVersions = flowVersionHistoryMapper.selectCount(wrapper);
        
        if (candidateVersions.size() >= totalVersions) {
            result.getWarnings().add("警告：将要归档所有版本，这可能不安全");
        }
        
        // 2. 检查是否包含最新版本
        LambdaQueryWrapper<FlowVersionHistory> maxWrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .orderByDesc(FlowVersionHistory::getVersion)
                .last("LIMIT 1");
        
        FlowVersionHistory latestVersion = flowVersionHistoryMapper.selectOne(maxWrapper);
        if (latestVersion != null && candidateVersions.contains(latestVersion.getVersion())) {
            result.getWarnings().add("警告：候选归档版本包含最新版本 v" + latestVersion.getVersion());
        }
        
        // 3. 检查是否有正在运行的执行
        LambdaQueryWrapper<FlowExecutionLog> runningWrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
                .in(FlowExecutionLog::getFlowVersion, candidateVersions)
                .eq(FlowExecutionLog::getStatus, FlowExecStatus.RUNNING);
        
        long runningCount = flowExecutionLogMapper.selectCount(runningWrapper);
        if (runningCount > 0) {
            result.getWarnings().add("警告：有" + runningCount + "个正在运行的执行使用了候选归档版本");
        }
    }

    /**
     * 执行归档前备份
     */
    private void performBackupBeforeArchive(Long flowId, List<Integer> candidateVersions, FlowArchiveResultDTO result) {
        log.info("Performing backup before archive for flow: {}, versions: {}", flowId, candidateVersions);
        
        try {
            // 1. 创建备份表和归档表（如果不存在）
            createBackupAndArchiveTableIfNotExists();
            
            // 2. 备份版本历史记录
            int versionHistoryBackupCount = backupVersionHistories(flowId, candidateVersions);
            
            // 3. 备份执行日志
            int executionLogBackupCount = backupExecutionLogs(flowId, candidateVersions);
            
            // 4. 记录备份信息
            Map<String, Object> backupInfo = new HashMap<>();
            backupInfo.put("backupTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            backupInfo.put("backupVersions", candidateVersions);
            backupInfo.put("versionHistoryBackupCount", versionHistoryBackupCount);
            backupInfo.put("executionLogBackupCount", executionLogBackupCount);
            // 备份位置信息，明确指定两个不同的备份表
            Map<String, String> backupLocations = new HashMap<>();
            backupLocations.put("versionHistoryBackup", "flow_version_history_backup");
            backupLocations.put("executionLogBackup", "flow_execution_log_backup");
            backupInfo.put("backupLocations", backupLocations);
            
            result.getStatistics().put("backup", backupInfo);
            log.info("Backup completed for flow: {}, versions: {}, history: {}, logs: {}", 
                    flowId, candidateVersions.size(), versionHistoryBackupCount, executionLogBackupCount);
                    
        } catch (Exception e) {
            log.warn("Backup failed for flow: {}, versions: {}", flowId, candidateVersions, e);
            result.getWarnings().add("备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建备份表和归档表（如果不存在）
     */
    private void createBackupAndArchiveTableIfNotExists() {
        try {
            flowArchiveMapper.createExecutionLogArchiveTable();
            flowArchiveMapper.createExecutionLogBackupTable();
            flowArchiveMapper.createVersionHistoryBackupTable();
            log.debug("Archive and backup tables created or already exist");
        } catch (Exception e) {
            log.warn("Failed to create archive and backup tables", e);
        }
    }
    
    /**
     * 备份版本历史记录
     */
    private int backupVersionHistories(Long flowId, List<Integer> versions) {
        if (versions.isEmpty()) {
            return 0;
        }
        
        try {
            flowArchiveMapper.backupVersionHistories(flowId, versions);
            // 查询备份的记录数
            LambdaQueryWrapper<FlowVersionHistory> wrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                    .eq(FlowVersionHistory::getFlowId, flowId)
                    .in(FlowVersionHistory::getVersion, versions);
            return flowVersionHistoryMapper.selectList(wrapper).size();
        } catch (Exception e) {
            log.warn("Failed to backup version histories for flow: {}, versions: {}", flowId, versions, e);
            return 0;
        }
    }
    
    /**
     * 备份执行日志
     */
    private int backupExecutionLogs(Long flowId, List<Integer> versions) {
        if (versions.isEmpty()) {
            return 0;
        }
        
        try {
            flowArchiveMapper.backupExecutionLogs(flowId, versions);
            // 查询备份的记录数
            LambdaQueryWrapper<FlowExecutionLog> wrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                    .eq(FlowExecutionLog::getFlowId, flowId)
                    .in(FlowExecutionLog::getFlowVersion, versions);
            return flowExecutionLogMapper.selectList(wrapper).size();
        } catch (Exception e) {
            log.warn("Failed to backup execution logs for flow: {}, versions: {}", flowId, versions, e);
            return 0;
        }
    }

    /**
     * 执行实际归档
     */
    private void performActualArchive(Long flowId, List<Integer> candidateVersions, FlowArchiveResultDTO result) {
        log.info("Performing actual archive for flow: {}, versions: {}, strategy: {}", 
                flowId, candidateVersions, result.getStrategy());
        
        List<Integer> successfullyArchived = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        
        String archiveStrategy = result.getStrategy().name();
        String archiveReason = String.format("基于[%s]策略自动归档", result.getStrategy().getDescription());
        
        for (Integer version : candidateVersions) {
            try {
                // 1. 归档版本历史记录
                archiveVersionHistory(flowId, version, archiveStrategy, archiveReason);
                
                // 2. 归档相关的执行日志
                archiveVersionExecutionLogs(flowId, version);
                
                successfullyArchived.add(version);
                log.debug("Successfully archived version {} for flow: {}", version, flowId);
                
            } catch (Exception e) {
                log.warn("Failed to archive version {} for flow: {}", version, flowId, e);
                skipped.add(version);
                result.getWarnings().add("版本 v" + version + " 归档失败: " + e.getMessage());
            }
        }
        
        result.setArchivedVersions(successfullyArchived);
        result.setSkippedVersions(skipped);
    }

    /**
     * 归档版本历史记录
     */
    private void archiveVersionHistory(Long flowId, Integer version, String archiveStrategy, String archiveReason) {
        // 获取要归档的版本历史记录
        LambdaQueryWrapper<FlowVersionHistory> wrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .eq(FlowVersionHistory::getVersion, version);
        
        FlowVersionHistory versionHistory = flowVersionHistoryMapper.selectOne(wrapper);
        if (versionHistory == null) {
            log.warn("未找到版本历史记录: flowId={}, version={}", flowId, version);
            return;
        }
        
        try {
            // 1. 创建归档记录
            FlowArchiveRecord flowArchiveRecord = FlowArchiveRecord.fromFlowVersionHistory(
                versionHistory, archiveStrategy, archiveReason);
            flowArchiveMapper.insert(flowArchiveRecord);
            
            // 2. 标记主表记录为已归档（软删除）
            versionHistory.setActive(false);
            flowVersionHistoryMapper.updateById(versionHistory);
            
            log.debug("成功归档版本历史记录: flowId={}, version={}", flowId, version);
        } catch (Exception e) {
            log.error("归档版本历史记录失败: flowId={}, version={}", flowId, version, e);
            throw new RuntimeException("归档版本历史记录失败", e);
        }
    }

    /**
     * 归档版本执行日志
     */
    private void archiveVersionExecutionLogs(Long flowId, Integer version) {
        // 获取该版本的执行日志
        LambdaQueryWrapper<FlowExecutionLog> wrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
                .eq(FlowExecutionLog::getFlowVersion, version);
        
        List<FlowExecutionLog> executionLogs = flowExecutionLogMapper.selectList(wrapper);
        if (executionLogs.isEmpty()) {
            log.debug("未找到执行日志: flowId={}, version={}", flowId, version);
            return;
        }
        
        try {
            // 1. 批量移动到归档表
            flowArchiveMapper.batchInsertExecutionLogArchives(executionLogs);

            // 2. 批量删除主表记录
            List<Long> logIds = executionLogs.stream()
                .map(FlowExecutionLog::getId)
                .collect(Collectors.toList());
            
            if (!logIds.isEmpty()) {
                flowExecutionLogMapper.deleteByIds(logIds);
            }
            
            log.debug("成功归档执行日志: flowId={}, version={}, count={}", 
                     flowId, version, executionLogs.size());
        } catch (Exception e) {
            log.error("归档执行日志失败: flowId={}, version={}", flowId, version, e);
            throw new RuntimeException("归档执行日志失败", e);
        }
    }

    /**
     * 生成归档统计信息
     */
    private void generateArchiveStatistics(Long flowId, FlowArchiveResultDTO result) {
        Map<String, Object> stats = result.getStatistics();
        
        stats.put("totalCandidates", result.getCandidateVersions().size());
        stats.put("successfullyArchived", result.getArchivedVersions().size());
        stats.put("skipped", result.getSkippedVersions().size());
        stats.put("warningCount", result.getWarnings().size());
        
        // 计算归档后的存储节省
        // 优化：直接使用已归档版本列表来计算，避免重复查询数据库
        long archivedExecutionLogs = 0;
        
        // 如果需要精确计算，可以查询已归档的执行日志数量
        // 这里使用一个估算值，避免大量数据库查询
        for (Integer version : result.getArchivedVersions()) {
            LambdaQueryWrapper<FlowExecutionLog> wrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                    .eq(FlowExecutionLog::getFlowId, flowId)
                    .eq(FlowExecutionLog::getFlowVersion, version);
            archivedExecutionLogs += flowExecutionLogMapper.selectCount(wrapper);
        }
        
        stats.put("archivedExecutionLogs", archivedExecutionLogs);
        stats.put("estimatedStorageSaved", calculateStorageSaved(result.getArchivedVersions().size(), archivedExecutionLogs));
    }

    /**
     * 计算存储节省估算
     */
    private String calculateStorageSaved(int archivedVersions, long archivedLogs) {
        // 简单估算：每个版本历史记录约1KB，每个执行日志约2KB
        long estimatedBytes = archivedVersions * 1024L + archivedLogs * 2048L;
        
        if (estimatedBytes < 1024) {
            return estimatedBytes + " B";
        } else if (estimatedBytes < 1024 * 1024) {
            return String.format("%.2f KB", estimatedBytes / 1024.0);
        } else {
            return String.format("%.2f MB", estimatedBytes / (1024.0 * 1024.0));
        }
    }

    /**
     * 获取归档建议
     * <p>基于流程的使用情况，提供归档建议</p>
     *
     * @param flowId 流程ID
     * @return 归档建议
     */
    public Map<String, Object> getArchiveRecommendations(Long flowId) {
        log.info("Generating archive recommendations for flow: {}", flowId);
        
        Map<String, Object> recommendations = new HashMap<>();
        
        // 1. 获取版本统计
        LambdaQueryWrapper<FlowVersionHistory> wrapper = new LambdaQueryWrapper<FlowVersionHistory>()
                .eq(FlowVersionHistory::getFlowId, flowId)
                .orderByDesc(FlowVersionHistory::getVersion);
        
        List<FlowVersionHistory> allVersions = flowVersionHistoryMapper.selectList(wrapper);
        recommendations.put("totalVersions", allVersions.size());
        
        if (allVersions.isEmpty()) {
            recommendations.put("recommendation", "NO_VERSIONS");
            recommendations.put("message", "暂无版本历史记录");
            return recommendations;
        }
        
        // 2. 分析版本年龄
        long currentTimeMillis = SystemClock.now();
        long ninetyDaysAgoMillis = currentTimeMillis - (90L * 24 * 60 * 60 * 1000);

        long oldVersions = allVersions.stream()
                .filter(v -> v.getCreateTime() != null && v.getCreateTime() < ninetyDaysAgoMillis)
                .count();
        
        // 3. 分析版本使用情况
        List<Map<String, Object>> usageStats = flowExecutionLogMapper.selectVersionExecutionStats(flowId);
        long unusedVersions = allVersions.size() - usageStats.size();
        
        // 4. 生成建议
        List<Map<String, Object>> suggestionList = new ArrayList<>();
        
        if (oldVersions > 5) {
            Map<String, Object> timeSuggestion = new HashMap<>();
            timeSuggestion.put("strategy", "BY_TIME");
            timeSuggestion.put("description", "建议归档90天前的版本");
            timeSuggestion.put("config", FlowArchiveConfigDTO.byTime(90));
            timeSuggestion.put("affectedVersions", oldVersions);
            suggestionList.add(timeSuggestion);
        }
        
        if (allVersions.size() > 20) {
            Map<String, Object> countSuggestion = new HashMap<>();
            countSuggestion.put("strategy", "BY_COUNT");
            countSuggestion.put("description", "建议只保留最新10个版本");
            countSuggestion.put("config", FlowArchiveConfigDTO.byCount(10));
            countSuggestion.put("affectedVersions", allVersions.size() - 10);
            suggestionList.add(countSuggestion);
        }
        
        if (unusedVersions > 3) {
            Map<String, Object> usageSuggestion = new HashMap<>();
            usageSuggestion.put("strategy", "BY_USAGE");
            usageSuggestion.put("description", "建议归档从未使用的版本");
            usageSuggestion.put("config", FlowArchiveConfigDTO.byUsage(1));
            usageSuggestion.put("affectedVersions", unusedVersions);
            suggestionList.add(usageSuggestion);
        }
        
        recommendations.put("suggestions", suggestionList);
        
        if (suggestionList.isEmpty()) {
            recommendations.put("recommendation", "NO_ARCHIVE_NEEDED");
            recommendations.put("message", "当前版本管理状况良好，暂不需要归档");
        } else {
            recommendations.put("recommendation", "ARCHIVE_RECOMMENDED");
            recommendations.put("message", "建议进行版本归档以优化存储和性能");
        }
        
        recommendations.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("Archive recommendations generated for flow: {}", flowId);
        return recommendations;
    }

    /**
     * 获取归档历史
     * <p>查询流程的归档操作历史</p>
     *
     * @param flowId 流程ID
     * @param limit 限制返回数量
     * @return 归档历史记录
     */
    public List<Map<String, Object>> getArchiveHistory(Long flowId, int limit) {
        log.info("Getting archive history for flow: {}, limit: {}", flowId, limit);
        
        try {
            // 查询归档操作日志表
            LambdaQueryWrapper<FlowArchiveOperationLog> wrapper = new LambdaQueryWrapper<FlowArchiveOperationLog>()
                    .eq(FlowArchiveOperationLog::getFlowId, flowId)
                    .orderByDesc(FlowArchiveOperationLog::getEndTime)
                    .last("LIMIT " + limit);
            
            List<FlowArchiveOperationLog> operationLogs = flowArchiveOperationLogMapper.selectList(wrapper);
            
            // 转换为Map格式返回
            List<Map<String, Object>> history = operationLogs.stream()
                    .map(log -> {
                        Map<String, Object> record = new HashMap<>();
                        record.put("operationId", log.getOperationId());
                        record.put("flowId", log.getFlowId());
                        record.put("version", log.getVersion());
                        record.put("operationType", log.getOperationType());
                        record.put("operationStatus", log.getOperationStatus());
                        record.put("operationTime", log.getStartTime());
                        record.put("durationMs", log.getDurationMs());
                        record.put("operatorId", log.getOperatorId());
                        return record;
                    })
                    .collect(Collectors.toList());
            
            log.info("Retrieved {} archive history records for flow: {}", history.size(), flowId);
            return history;
        } catch (Exception e) {
            log.error("查询归档历史失败: flowId={}", flowId, e);
            return Collections.emptyList();
        }
    }
}