package cn.fxbin.bubble.flow.core.mapper;

import cn.fxbin.bubble.data.mybatisplus.mapper.BaseMapperX;
import cn.fxbin.bubble.flow.core.model.entity.FlowArchiveRecord;
import cn.fxbin.bubble.flow.core.model.entity.FlowExecutionLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 归档数据访问层
 * 
 * @author fxbin
 * @since 2025-09-05 17:58
 */
@Mapper
public interface FlowArchiveMapper extends BaseMapperX<FlowArchiveRecord> {

    /**
     * 批量插入执行日志归档记录
     * @param executionLogs 执行日志列表
     */
    @Insert("<script>" +
           "INSERT INTO flow_execution_log_archive " +
           "(id, flow_id, flow_instance_id, flow_definition_id, flow_version, dataset_ids, trigger_type, " +
           "trigger_by, start_time, end_time, duration_ms, status, error_message, input_parameters, " +
           "output_results, create_time, update_time, tenant_id, remark) " +
           "VALUES " +
           "<foreach collection='list' item='log' separator=','>" +
           "(#{log.id}, #{log.flowId}, #{log.flowInstanceId}, #{log.flowDefinitionId}, #{log.flowVersion}, " +
           "#{log.datasetIds, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, " +
           "#{log.triggerType}, #{log.triggerBy}, #{log.startTime}, #{log.endTime}, #{log.durationMs}, " +
           "#{log.status}, #{log.errorMessage}, #{log.inputParameters}, #{log.outputResults}, " +
           "#{log.createTime}, #{log.updateTime}, #{log.tenantId}, #{log.remark})" +
           "</foreach>" +
           "</script>")
    void batchInsertExecutionLogArchives(@Param("list") List<FlowExecutionLog> executionLogs);
    
    /**
     * 创建执行日志归档表（如果不存在）
     */
    @Select("CREATE TABLE IF NOT EXISTS flow_execution_log_archive LIKE flow_execution_log")
    void createExecutionLogArchiveTable();
    
    /**
     * 创建执行日志备份表（如果不存在）
     */
    @Select("CREATE TABLE IF NOT EXISTS flow_execution_log_backup LIKE flow_execution_log")
    void createExecutionLogBackupTable();
    
    /**
     * 创建版本历史备份表（如果不存在）
     */
    @Select("CREATE TABLE IF NOT EXISTS flow_version_history_backup LIKE flow_version_history")
    void createVersionHistoryBackupTable();
    
    /**
     * 备份执行日志到备份表
     * @param flowId 流程ID
     * @param versions 版本列表
     */
    @Insert("<script>" +
           "INSERT INTO flow_execution_log_backup " +
           "SELECT * FROM flow_execution_log " +
           "WHERE flow_id = #{flowId} " +
           "AND flow_version IN " +
           "<foreach item='version' collection='versions' open='(' separator=',' close=')'>" +
           "#{version}" +
           "</foreach>" +
           "</script>")
    void backupExecutionLogs(@Param("flowId") Long flowId, @Param("versions") List<Integer> versions);
    
    /**
     * 备份版本历史到备份表
     * @param flowId 流程ID
     * @param versions 版本列表
     */
    @Insert("<script>" +
           "INSERT INTO flow_version_history_backup " +
           "SELECT * FROM flow_version_history " +
           "WHERE flow_id = #{flowId} " +
           "AND version IN " +
           "<foreach item='version' collection='versions' open='(' separator=',' close=')'>" +
           "#{version}" +
           "</foreach>" +
           "</script>")
    void backupVersionHistories(@Param("flowId") Long flowId, @Param("versions") List<Integer> versions);
}