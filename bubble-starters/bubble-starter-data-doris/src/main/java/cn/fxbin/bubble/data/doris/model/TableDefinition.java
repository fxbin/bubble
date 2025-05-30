package cn.fxbin.bubble.data.doris.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * TableDefinition
 * Doris 表定义
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
public class TableDefinition {

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 列定义列表
     */
    private List<ColumnDefinition> columnDefinitions;

    /**
     * Key 类型
     */
    private KeysType keysType;

    /**
     * 排序键列表 (仅适用于 DUPLICATE KEY 和 AGGREGATE KEY 模型)
     * 例如: `k1, k2`
     */
    private List<String> distributedKeys;

    /**
     * 引擎类型, 例如: olap, mysql
     */
    private String engine = "olap";

    /**
     * 分区定义
     */
    private PartitionDefinition partitionDefinition;

    /**
     * 分桶列，多个列用逗号分隔
     * 例如: `k3` 或 `k3, k4`
     */
    private List<String> distributedBy;

    /**
     * 分桶数量
     */
    private int buckets;

    /**
     * 表的属性，例如: "replication_num" = "3", "dynamic_partition.enable" = "true"
     */
    private Map<String, String> properties;

    /**
     * 表注释
     */
    private String comment;

}