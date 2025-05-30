package cn.fxbin.bubble.data.doris.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * PartitionDefinition
 * Doris 分区定义
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class PartitionDefinition {

    /**
     * 分区类型
     */
    private PartitionType type;

    /**
     * 分区列或表达式，多个列用逗号分隔
     * 例如：对于 RANGE 分区，可以是 `k1` 或 `date_trunc('month', k1)`
     * 对于 LIST 分区，可以是 `k1` 或 `k1,k2`
     */
    private String partitionBy;

    /**
     * 手动分区的具体定义 (仅当 type 为 MANUAL 时有效)
     * 格式如：PARTITION p1 VALUES LESS THAN ("2023-01-01"), PARTITION p2 VALUES LESS THAN ("2023-02-01")
     * 或 PARTITION p_a VALUES IN ("a"), PARTITION p_b VALUES IN ("b")
     * Key: 分区名称, Value: 分区值定义 (例如: VALUES LESS THAN ('2023-01-01'))
     */
    private Map<String, String> manualPartitions;

    /**
     * 动态分区属性 (仅当 type 为 DYNAMIC 时有效)
     */
    private DynamicPartition dynamicPartition;

    /**
     * 自动分区属性 (仅当 type 为 AUTO 时有效)
     */
    private AutoPartition autoPartition;

    /**
     * 分区类型枚举
     */
    public enum PartitionType {
        /**
         * 手动分区
         */
        MANUAL,
        /**
         * 动态分区
         */
        DYNAMIC,
        /**
         * 自动分区 (在 DDL 中指定 AUTO PARTITION BY)
         */
        AUTO
    }

    @Data
    @Builder
    @Accessors(chain = true)
    public static class DynamicPartition {
        /**
         * 是否启用动态分区
         */
        private boolean enable;
        /**
         * 动态分区类型 (TIME / HASH)
         */
        private String type;
        /**
         * 动态分区起始偏移量
         */
        private int start;
        /**
         * 动态分区结束偏移量
         */
        private int end;
        /**
         * 动态分区单位 (HOUR, DAY, WEEK, MONTH)
         */
        private String unit;
        /**
         * 动态分区前缀
         */
        private String prefix;
        /**
         * 动态分区间隔 (默认为1)
         */
        private int every;
        /**
         * 动态分区时区
         */
        private String timeZone;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    public static class AutoPartition {
        /**
         * 自动分区类型 (RANGE / LIST)
         */
        private String type;
        /**
         * 自动分区表达式或列
         * 对于 RANGE: date_trunc(column, 'granularity')
         * 对于 LIST: column 或 column1,column2
         */
        private String expression;
        /**
         * 是否允许 LIST 分区列中包含 NULL 值。
         * 仅适用于 LIST 类型。
         */
        private boolean allowPartitionColumnNullable;
    }
}