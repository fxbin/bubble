package cn.fxbin.bubble.flow.core.mapper;

import cn.fxbin.bubble.data.mybatisplus.mapper.BaseMapperX;
import cn.fxbin.bubble.flow.core.model.entity.FlowNodeExecutionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 节点执行日志 Mapper 接口
 * <p>提供对节点执行日志表 (node_execution_log) 的基本 CRUD 操作。</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024-07-31
 */
@Mapper
public interface FlowNodeExecutionLogMapper extends BaseMapperX<FlowNodeExecutionLog> {

    // 可以在此添加自定义的 Mapper 方法

}