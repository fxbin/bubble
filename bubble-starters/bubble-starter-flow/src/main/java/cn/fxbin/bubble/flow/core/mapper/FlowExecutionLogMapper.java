package cn.fxbin.bubble.flow.core.mapper;

import cn.fxbin.bubble.data.mybatisplus.mapper.BaseMapperX;
import cn.fxbin.bubble.flow.core.model.entity.FlowExecutionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 工作流执行日志 Mapper 接口
 * <p>提供对工作流执行日志表 (flow_execution_log) 的基本 CRUD 操作。</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024-07-31
 */
@Mapper
public interface FlowExecutionLogMapper extends BaseMapperX<FlowExecutionLog> {

    /**
     * 查询流程各版本的执行统计信息
     * <p>统计每个版本的执行次数、成功次数、失败次数等</p>
     *
     * @param flowId 流程ID
     * @return 版本执行统计列表，包含版本号、执行次数、成功次数、失败次数
     */
    @Select("SELECT flow_version as version, " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount, " +
            "SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failedCount, " +
            "AVG(TIMESTAMPDIFF(SECOND, start_time, end_time)) as avgDurationSeconds " +
            "FROM flow_execution_log " +
            "WHERE flow_id = #{flowId} AND flow_version IS NOT NULL " +
            "GROUP BY flow_version " +
            "ORDER BY flow_version DESC")
    List<Map<String, Object>> selectVersionExecutionStats(@Param("flowId") Long flowId);

    /**
     * 查询指定版本的最近执行记录
     * <p>用于版本性能分析和问题排查</p>
     *
     * @param flowId 流程ID
     * @param version 版本号
     * @param limit 限制条数
     * @return 最近执行记录列表
     */
    @Select("SELECT * FROM flow_execution_log " +
            "WHERE flow_id = #{flowId} AND flow_version = #{version} " +
            "ORDER BY start_time DESC " +
            "LIMIT #{limit}")
    List<FlowExecutionLog> selectRecentExecutionsByVersion(@Param("flowId") Long flowId, 
                                                          @Param("version") Integer version, 
                                                          @Param("limit") Integer limit);

    /**
     * 查询需要归档的历史执行记录
     * <p>用于数据清理和归档，查询指定时间之前的记录</p>
     *
     * @param flowId 流程ID
     * @param beforeDays 多少天之前的记录
     * @param limit 限制条数
     * @return 需要归档的执行记录列表
     */
    @Select("SELECT * FROM flow_execution_log " +
            "WHERE flow_id = #{flowId} " +
            "AND start_time < DATE_SUB(NOW(), INTERVAL #{beforeDays} DAY) " +
            "ORDER BY start_time ASC " +
            "LIMIT #{limit}")
    List<FlowExecutionLog> selectExecutionsForArchive(@Param("flowId") Long flowId, 
                                                     @Param("beforeDays") Integer beforeDays, 
                                                     @Param("limit") Integer limit);

}