package cn.fxbin.bubble.data.doris;

import org.springframework.lang.Nullable;

/**
 * DorisDdlOperations
 * 提供 Doris 数据库通用的 DDL 操作接口，例如执行原生 SQL，检查表/分区是否存在等。
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
public interface DorisDdlOperations {

    /**
     * 执行原生 DDL 语句
     *
     * @param ddl SQL DDL语句
     */
    void execute(String ddl);

    /**
     * 检查表是否存在
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 如果表存在则返回 true，否则返回 false
     */
    boolean tableExists(@Nullable String databaseName, String tableName);

    /**
     * 检查分区是否存在
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param partitionName 分区名称
     * @return 如果分区存在则返回 true，否则返回 false
     */
    boolean partitionExists(@Nullable String databaseName, String tableName, String partitionName);

    /**
     * 创建分区
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param partitionDefinition 分区定义，例如: PARTITION p202301 VALUES LESS THAN ('2023-02-01')
     */
    void addPartition(@Nullable String databaseName, String tableName, String partitionDefinition);

    /**
     * 删除分区
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param partitionName 分区名称
     */
    void dropPartition(@Nullable String databaseName, String tableName, String partitionName);

    /**
     * 获取自动创建的分区名称，根据传入的日期值
     *
     * @param dateValue 日期值
     * @param granularity 分区粒度 (year, month, day)
     * @return 分区名称
     */
    String getAutoPartitionName(String dateValue, String granularity);

}