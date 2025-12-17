package cn.fxbin.bubble.flow.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * PluginType
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/23 9:50
 */
@Getter
@AllArgsConstructor
public enum PluginType implements Serializable {

    /**
     * 开始节点
     */
    START_NODE(1, "START", "工作流的起始节点，用于设定启动工作流需要的信息"),

    /**
     * 结束节点
     */
    END_NODE(2, "END", "工作流的最终节点，用于返回工作流运行后的结果信息"),

    /**
     * HTTP 节点
     */
    HTTP_NODE(100, "HTTP", "HTTP 节点"),

    /**
     * 代码节点
     */
    CODE_NODE(101, "CODE", "代码节点"),

    /**
     * 预处理方法节点
     */
    PREPROCESS_NODE(3, "PREPROCESS_METHOD", "根据数据集中的参数特征，结合数据探索任务需求，对原始数据进行清理、转换和组织。"),

    /**
     * 特征值节点
     */
    EIGENVALUE_NODE(4, "EIGENVALUE", "根据数据集中的参数特征，结合数据探索任务需求，对原始数据进行清理、转换和组织。"),

    /**
     * 预警规则节点
     */
    WARNING_RULE_NODE(5, "WARNING_RULE", "根据不同的业务需求，对参数或特征值建⽴规则指标，如果超过制定指标，会触发预警。"),

    /**
     * 模型节点
     */
    MODEL_NODE(6, "MODEL", "利用AI模型去进行数据探索，模型节点通常有限定的数据集参数和方法节点，无法变更。"),

    /**
     * 计算节点
     */
    COMPUTE_NODE(7, "COMPUTE", "在数据探索的各阶段，对参数或特征值进行计算分析，输出计算指标。"),
    /**
     * 评估节点
     */
    EVALUATION_NODE(11,"EVALUATION","训练平台评估节点"),
    /**
     * 预测节点
     */
    PREDICTION_NODE(12,"PREDICTION","训练平台预测节点"),
    /**
     * 基础模型节点
     */
    TRAIN_BASE_MODEL_NODE(13,"TRAIN_BASE_MODEL","训练平台待训练的基础模型")
    ;
    
//    /**
//     * 过滤节点
//     */
//    FILTER_NODE(8, "FILTER", "对数据集进行条件过滤，筛选符合特定条件的数据。"),
//
//    /**
//     * 连接节点
//     */
//    JOIN_NODE(9, "JOIN", "将多个数据集按照指定的关联条件进行连接操作。"),
//
//    /**
//     * 聚合节点
//     */
//    AGGREGATE_NODE(10, "AGGREGATE", "对数据集进行分组聚合操作，如求和、平均值、最大值等。");


    /**
     * 节点类型编码
     */
    @JsonValue
    @EnumValue
    private final int code;

    /**
     * 节点类型名称
     */
    private final String name;

    /**
     * 节点类型描述
     */
    private final String desc;

}
