package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.model.ColumnDefinition;
import cn.fxbin.bubble.data.doris.model.TableDefinition;
import org.springframework.lang.Nullable;

/**
 * DorisTableDdlOperations
 * 提供 Doris 表级的 DDL 操作接口，包括创建、删除、修改表结构等。
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
public interface DorisTableDdlOperations {

    /**
     * 根据 TableDefinition 对象创建 Doris 表。
     * 该方法会智能地构建复杂的 CREATE TABLE DDL 语句，全面支持列定义、表引擎、
     * 数据模型（KeysType）、手动与动态分区策略、分桶策略以及自定义表属性。
     *
     * @param tableDefinition 表定义对象
     */
    void createTable(TableDefinition tableDefinition);

    /**
     * 删除 Doris 表。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     */
    void dropTable(@Nullable String databaseName, String tableName);

    /**
     * 重命名 Doris 表。
     *
     * @param databaseName 数据库名称
     * @param oldTableName 旧表名称
     * @param newTableName 新表名称
     */
    void renameTable(@Nullable String databaseName, String oldTableName, String newTableName);

    /**
     * 修改表名。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param newTableName 新表名称
     */
    void alterTableName(@Nullable String databaseName, String tableName, String newTableName);

    /**
     * 为 Doris 表添加新列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnDefinition 新列定义
     */
    void addColumn(@Nullable String databaseName, String tableName, ColumnDefinition columnDefinition);

    /**
     * 修改 Doris 表列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnDefinition 列定义
     */
    void modifyColumn(@Nullable String databaseName, String tableName, ColumnDefinition columnDefinition);

    /**
     * 删除 Doris 表的列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnName 要删除的列名
     */
    void dropColumn(@Nullable String databaseName, String tableName, String columnName);

    /**
     * 排序 Doris 表的列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnName 要排序的列名
     * @param afterColumnName 排序到指定列之后，如果为 null，则排到第一个
     */
    void orderColumn(@Nullable String databaseName, String tableName, String columnName, @Nullable String afterColumnName);

    /**
     * 创建 Doris 表索引。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param indexName 索引名称
     * @param indexColumns 索引列，多个列用逗号分隔
     * @param indexType 索引类型，例如 BITMAP, NGRAM BF
     * @param comment 索引注释
     */
    void createIndex(@Nullable String databaseName, String tableName, String indexName, String indexColumns, String indexType, @Nullable String comment);

    /**
     * 删除 Doris 表索引。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param indexName 索引名称
     */
    void dropIndex(@Nullable String databaseName, String tableName, String indexName);

    /**
     * 启用或禁用表的 AUTO PARTITION 功能。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param enable 是否启用
     */
    void enableAutoPartition(@Nullable String databaseName, String tableName, boolean enable);

}