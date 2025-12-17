package cn.fxbin.bubble.flow.core.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.fxbin.bubble.flow.core.enums.FlowExecStatus;
import cn.fxbin.bubble.flow.core.mapper.FlowExecutionLogMapper;
import cn.fxbin.bubble.flow.core.model.entity.FlowExecutionLog;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程版本分析服务
 * <p>提供版本性能分析、趋势统计、健康度评估等功能</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowVersionAnalyticsProvider {

    private final FlowExecutionLogMapper flowExecutionLogMapper;

    /**
     * 版本性能统计
     */

    @Data
    public static class VersionPerformanceStats {
        private Integer version;
        private Long totalExecutions;
        private Long successExecutions;
        private Long failedExecutions;
        private Double successRate;
        private Double avgDurationSeconds;
        private Double minDurationSeconds;
        private Double maxDurationSeconds;
        private LocalDateTime firstExecutionTime;
        private LocalDateTime lastExecutionTime;
        private String healthStatus;
    }

    /**
     * 版本趋势分析结果
     */
    @Data
    public static class VersionTrendAnalysis {
        private Long flowId;
        private List<VersionPerformanceStats> versionStats;
        private Map<String, Object> trendIndicators;
        private List<String> recommendations;

        public VersionTrendAnalysis(Long flowId) {
            this.flowId = flowId;
            this.versionStats = new ArrayList<>();
            this.trendIndicators = new HashMap<>();
            this.recommendations = new ArrayList<>();
        }
    }

    /**
     * 获取流程所有版本的性能统计
     * <p>分析每个版本的执行情况、成功率、性能指标等</p>
     *
     * @param flowId 流程ID
     * @return 版本性能统计列表
     */
    public List<VersionPerformanceStats> getVersionPerformanceStats(Long flowId) {
        log.info("Analyzing version performance for flow: {}", flowId);

        // 1. 获取版本执行统计数据
        List<Map<String, Object>> rawStats = flowExecutionLogMapper.selectVersionExecutionStats(flowId);
        
        List<VersionPerformanceStats> stats = new ArrayList<>();
        
        for (Map<String, Object> rawStat : rawStats) {
            VersionPerformanceStats stat = new VersionPerformanceStats();
            
            // 基础统计数据
            stat.setVersion((Integer) rawStat.get("version"));
            stat.setTotalExecutions(((Number) rawStat.get("totalCount")).longValue());
            stat.setSuccessExecutions(((Number) rawStat.get("successCount")).longValue());
            stat.setFailedExecutions(((Number) rawStat.get("failedCount")).longValue());
            
            // 计算成功率
            if (stat.getTotalExecutions() > 0) {
                double successRate = (double) stat.getSuccessExecutions() / stat.getTotalExecutions() * 100;
                stat.setSuccessRate(BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            } else {
                stat.setSuccessRate(0.0);
            }
            
            // 性能指标
            Object avgDuration = rawStat.get("avgDurationSeconds");
            if (avgDuration != null) {
                stat.setAvgDurationSeconds(((Number) avgDuration).doubleValue());
            }
            
            // 2. 获取详细执行时间信息
            enrichWithDetailedStats(flowId, stat);
            
            // 3. 评估健康状态
            stat.setHealthStatus(evaluateVersionHealth(stat));
            
            stats.add(stat);
        }
        
        // 按版本号排序
        stats.sort(Comparator.comparing(VersionPerformanceStats::getVersion).reversed());
        
        log.info("Analyzed {} versions for flow: {}", stats.size(), flowId);
        return stats;
    }

    /**
     * 丰富版本统计的详细信息
     */
    private void enrichWithDetailedStats(Long flowId, VersionPerformanceStats stat) {
        LambdaQueryWrapper<FlowExecutionLog> wrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
                .eq(FlowExecutionLog::getFlowVersion, stat.getVersion())
                .isNotNull(FlowExecutionLog::getStartTime)
                .isNotNull(FlowExecutionLog::getEndTime)
                .orderByAsc(FlowExecutionLog::getStartTime);

        List<FlowExecutionLog> executions = flowExecutionLogMapper.selectList(wrapper);
        
        if (!executions.isEmpty()) {
            // 计算执行时间统计
            List<Double> durations = executions.stream()
                    .filter(log -> log.getStartTime() != null && log.getEndTime() != null)
                    .map(log -> {
                        long durationMillis = java.time.Duration.between(log.getStartTime(), log.getEndTime()).toMillis();
                        return durationMillis / 1000.0; // 转换为秒
                    })
                    .toList();

            if (!durations.isEmpty()) {
                stat.setMinDurationSeconds(durations.stream().min(Double::compare).orElse(0.0));
                stat.setMaxDurationSeconds(durations.stream().max(Double::compare).orElse(0.0));
                
                if (stat.getAvgDurationSeconds() == null) {
                    double avgDuration = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    stat.setAvgDurationSeconds(BigDecimal.valueOf(avgDuration).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
            }
            
            // 设置首次和最后执行时间
            stat.setFirstExecutionTime(executions.get(0).getStartTime());
            stat.setLastExecutionTime(executions.get(executions.size() - 1).getStartTime());
        }
    }

    /**
     * 评估版本健康状态
     */
    private String evaluateVersionHealth(VersionPerformanceStats stat) {
        if (stat.getTotalExecutions() == 0) {
            return "UNKNOWN";
        }
        
        double successRate = stat.getSuccessRate();
        
        if (successRate >= 95.0) {
            return "EXCELLENT";
        } else if (successRate >= 85.0) {
            return "GOOD";
        } else if (successRate >= 70.0) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    /**
     * 分析版本趋势
     * <p>分析版本间的性能变化趋势，提供优化建议</p>
     *
     * @param flowId 流程ID
     * @return 版本趋势分析结果
     */
    public VersionTrendAnalysis analyzeVersionTrend(Long flowId) {
        log.info("Analyzing version trend for flow: {}", flowId);
        
        VersionTrendAnalysis analysis = new VersionTrendAnalysis(flowId);
        
        // 1. 获取版本性能统计
        List<VersionPerformanceStats> versionStats = getVersionPerformanceStats(flowId);
        analysis.setVersionStats(versionStats);
        
        if (versionStats.size() < 2) {
            log.warn("Insufficient version data for trend analysis. Flow: {}, Versions: {}", flowId, versionStats.size());
            analysis.getRecommendations().add("需要更多版本数据才能进行趋势分析");
            return analysis;
        }
        
        // 2. 计算趋势指标
        calculateTrendIndicators(versionStats, analysis);
        
        // 3. 生成优化建议
        generateRecommendations(versionStats, analysis);
        
        log.info("Version trend analysis completed for flow: {}", flowId);
        return analysis;
    }

    /**
     * 计算趋势指标
     */
    private void calculateTrendIndicators(List<VersionPerformanceStats> versionStats, VersionTrendAnalysis analysis) {
        Map<String, Object> indicators = analysis.getTrendIndicators();
        
        // 按版本号排序（升序）
        List<VersionPerformanceStats> sortedStats = versionStats.stream()
                .sorted(Comparator.comparing(VersionPerformanceStats::getVersion))
                .collect(Collectors.toList());
        
        // 成功率趋势
        List<Double> successRates = sortedStats.stream()
                .map(VersionPerformanceStats::getSuccessRate)
                .collect(Collectors.toList());
        indicators.put("successRateTrend", calculateTrend(successRates));
        indicators.put("latestSuccessRate", successRates.get(successRates.size() - 1));
        indicators.put("avgSuccessRate", successRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        
        // 性能趋势
        List<Double> avgDurations = sortedStats.stream()
                .map(VersionPerformanceStats::getAvgDurationSeconds)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!avgDurations.isEmpty()) {
            indicators.put("performanceTrend", calculateTrend(avgDurations));
            indicators.put("latestAvgDuration", avgDurations.get(avgDurations.size() - 1));
            indicators.put("bestAvgDuration", avgDurations.stream().min(Double::compare).orElse(0.0));
        }
        
        // 执行量趋势
        List<Double> executionCounts = sortedStats.stream()
                .map(stat -> stat.getTotalExecutions().doubleValue())
                .collect(Collectors.toList());
        indicators.put("executionVolumeTrend", calculateTrend(executionCounts));
        indicators.put("totalExecutions", executionCounts.stream().mapToDouble(Double::doubleValue).sum());
        
        // 版本稳定性评分
        double stabilityScore = calculateStabilityScore(sortedStats);
        indicators.put("stabilityScore", stabilityScore);
    }

    /**
     * 计算数值序列的趋势（正值表示上升，负值表示下降）
     */
    private double calculateTrend(List<Double> values) {
        if (values.size() < 2) {
            return 0.0;
        }
        
        double firstValue = values.get(0);
        double lastValue = values.get(values.size() - 1);
        
        if (firstValue == 0) {
            return 0.0;
        }
        
        return ((lastValue - firstValue) / firstValue) * 100; // 百分比变化
    }

    /**
     * 计算版本稳定性评分
     */
    private double calculateStabilityScore(List<VersionPerformanceStats> stats) {
        if (stats.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int validVersions = 0;
        
        for (VersionPerformanceStats stat : stats) {
            if (stat.getTotalExecutions() > 0) {
                double versionScore = 0.0;
                
                // 成功率权重 60%
                versionScore += (stat.getSuccessRate() / 100.0) * 0.6;
                
                // 执行量权重 20%（归一化处理）
                double executionScore = Math.min(stat.getTotalExecutions() / 100.0, 1.0);
                versionScore += executionScore * 0.2;
                
                // 性能稳定性权重 20%（基于平均执行时间的倒数）
                if (stat.getAvgDurationSeconds() != null && stat.getAvgDurationSeconds() > 0) {
                    double performanceScore = Math.min(60.0 / stat.getAvgDurationSeconds(), 1.0); // 60秒作为基准
                    versionScore += performanceScore * 0.2;
                }
                
                totalScore += versionScore;
                validVersions++;
            }
        }
        
        return validVersions > 0 ? (totalScore / validVersions) * 100 : 0.0;
    }

    /**
     * 生成优化建议
     */
    private void generateRecommendations(List<VersionPerformanceStats> versionStats, VersionTrendAnalysis analysis) {
        List<String> recommendations = analysis.getRecommendations();
        Map<String, Object> indicators = analysis.getTrendIndicators();
        
        // 最新版本
        VersionPerformanceStats latestVersion = versionStats.stream()
                .max(Comparator.comparing(VersionPerformanceStats::getVersion))
                .orElse(null);
        
        if (latestVersion == null) return;
        
        // 成功率建议
        double successRateTrend = (Double) indicators.getOrDefault("successRateTrend", 0.0);
        if (latestVersion.getSuccessRate() < 90.0) {
            recommendations.add("当前版本成功率较低（" + latestVersion.getSuccessRate() + "%），建议检查错误日志并优化流程逻辑");
        }
        
        if (successRateTrend < -5.0) {
            recommendations.add("成功率呈下降趋势（" + String.format("%.1f", successRateTrend) + "%），建议回滚到稳定版本或紧急修复");
        }
        
        // 性能建议
        Double performanceTrend = (Double) indicators.get("performanceTrend");
        if (performanceTrend != null && performanceTrend > 20.0) {
            recommendations.add("执行时间呈上升趋势（" + String.format("%.1f", performanceTrend) + "%），建议进行性能优化");
        }
        
        if (latestVersion.getAvgDurationSeconds() != null && latestVersion.getAvgDurationSeconds() > 300) {
            recommendations.add("平均执行时间过长（" + String.format("%.1f", latestVersion.getAvgDurationSeconds()) + "秒），建议优化流程节点或并行处理");
        }
        
        // 稳定性建议
        double stabilityScore = (Double) indicators.getOrDefault("stabilityScore", 0.0);
        if (stabilityScore < 70.0) {
            recommendations.add("整体稳定性评分较低（" + String.format("%.1f", stabilityScore) + "分），建议加强测试和监控");
        }
        
        // 版本管理建议
        if (versionStats.size() > 10) {
            long oldVersions = versionStats.stream()
                    .filter(stat -> stat.getLastExecutionTime() != null)
                    .filter(stat -> stat.getLastExecutionTime().isBefore(LocalDateTime.now().minusDays(30)))
                    .count();
            
            if (oldVersions > 5) {
                recommendations.add("存在" + oldVersions + "个超过30天未使用的版本，建议清理历史版本以优化存储");
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("流程版本运行状况良好，继续保持当前的开发和运维实践");
        }
    }

    /**
     * 获取版本健康度报告
     * <p>生成版本健康度的综合报告</p>
     *
     * @param flowId 流程ID
     * @return 健康度报告
     */
    public Map<String, Object> getVersionHealthReport(Long flowId) {
        log.info("Generating version health report for flow: {}", flowId);
        
        Map<String, Object> report = new HashMap<>();
        
        // 1. 基础统计
        List<VersionPerformanceStats> versionStats = getVersionPerformanceStats(flowId);
        report.put("totalVersions", versionStats.size());
        
        if (versionStats.isEmpty()) {
            report.put("status", "NO_DATA");
            report.put("message", "暂无版本执行数据");
            return report;
        }
        
        // 2. 当前版本状态
        VersionPerformanceStats currentVersion = versionStats.stream()
                .max(Comparator.comparing(VersionPerformanceStats::getVersion))
                .orElse(null);
        
        if (currentVersion != null) {
            Map<String, Object> currentStatus = new HashMap<>();
            currentStatus.put("version", currentVersion.getVersion());
            currentStatus.put("successRate", currentVersion.getSuccessRate());
            currentStatus.put("healthStatus", currentVersion.getHealthStatus());
            currentStatus.put("totalExecutions", currentVersion.getTotalExecutions());
            currentStatus.put("avgDurationSeconds", currentVersion.getAvgDurationSeconds());
            report.put("currentVersion", currentStatus);
        }
        
        // 3. 整体健康指标
        Map<String, Object> healthMetrics = new HashMap<>();
        
        // 平均成功率
        double avgSuccessRate = versionStats.stream()
                .mapToDouble(VersionPerformanceStats::getSuccessRate)
                .average().orElse(0.0);
        healthMetrics.put("avgSuccessRate", BigDecimal.valueOf(avgSuccessRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
        
        // 健康版本占比
        long healthyVersions = versionStats.stream()
                .filter(stat -> "EXCELLENT".equals(stat.getHealthStatus()) || "GOOD".equals(stat.getHealthStatus()))
                .count();
        double healthyRatio = (double) healthyVersions / versionStats.size() * 100;
        healthMetrics.put("healthyVersionRatio", BigDecimal.valueOf(healthyRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        
        // 总执行次数
        long totalExecutions = versionStats.stream()
                .mapToLong(VersionPerformanceStats::getTotalExecutions)
                .sum();
        healthMetrics.put("totalExecutions", totalExecutions);
        
        report.put("healthMetrics", healthMetrics);
        
        // 4. 版本分布
        Map<String, Long> healthDistribution = versionStats.stream()
                .collect(Collectors.groupingBy(
                        VersionPerformanceStats::getHealthStatus,
                        Collectors.counting()
                ));
        report.put("healthDistribution", healthDistribution);
        
        // 5. 趋势分析
        if (versionStats.size() >= 2) {
            VersionTrendAnalysis trendAnalysis = analyzeVersionTrend(flowId);
            report.put("trendIndicators", trendAnalysis.getTrendIndicators());
            report.put("recommendations", trendAnalysis.getRecommendations());
        }
        
        // 6. 生成时间
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("Version health report generated for flow: {}", flowId);
        return report;
    }

    /**
     * 获取版本执行历史摘要
     * <p>获取指定时间范围内的版本执行摘要信息</p>
     *
     * @param flowId 流程ID
     * @param days 查询天数
     * @return 执行历史摘要
     */
    public Map<String, Object> getVersionExecutionSummary(Long flowId, int days) {
        log.info("Getting version execution summary for flow: {}, days: {}", flowId, days);
        
        Map<String, Object> summary = new HashMap<>();
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 查询指定时间范围内的执行记录
        LambdaQueryWrapper<FlowExecutionLog> wrapper = new LambdaQueryWrapper<FlowExecutionLog>()
                .eq(FlowExecutionLog::getFlowId, flowId)
                .ge(FlowExecutionLog::getStartTime, startTime)
                .orderByDesc(FlowExecutionLog::getStartTime);
        
        List<FlowExecutionLog> recentExecutions = flowExecutionLogMapper.selectList(wrapper);
        
        if (recentExecutions.isEmpty()) {
            summary.put("status", "NO_RECENT_DATA");
            summary.put("message", "最近" + days + "天内无执行记录");
            return summary;
        }
        
        // 按版本分组统计
        Map<Integer, List<FlowExecutionLog>> versionGroups = recentExecutions.stream()
                .filter(log -> log.getFlowVersion() != null)
                .collect(Collectors.groupingBy(FlowExecutionLog::getFlowVersion));
        
        List<Map<String, Object>> versionSummaries = new ArrayList<>();
        
        for (Map.Entry<Integer, List<FlowExecutionLog>> entry : versionGroups.entrySet()) {
            Integer version = entry.getKey();
            List<FlowExecutionLog> executions = entry.getValue();
            
            Map<String, Object> versionSummary = new HashMap<>();
            versionSummary.put("version", version);
            versionSummary.put("executionCount", executions.size());
            
            // 成功率统计
            long successCount = executions.stream()
                    .filter(log -> FlowExecStatus.SUCCESS.equals(log.getStatus()))
                    .count();
            double successRate = (double) successCount / executions.size() * 100;
            versionSummary.put("successRate", BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            
            // 最近执行时间
            Optional<LocalDateTime> latestExecution = executions.stream()
                    .map(FlowExecutionLog::getStartTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo);
            latestExecution.ifPresent(time -> versionSummary.put("latestExecution", time));
            
            versionSummaries.add(versionSummary);
        }
        
        // 按版本号排序
        versionSummaries.sort((a, b) -> Integer.compare((Integer) b.get("version"), (Integer) a.get("version")));
        
        summary.put("timeRange", days + " days");
        summary.put("totalExecutions", recentExecutions.size());
        summary.put("activeVersions", versionGroups.size());
        summary.put("versionSummaries", versionSummaries);
        summary.put("queryTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("Version execution summary completed for flow: {}", flowId);
        return summary;
    }
}