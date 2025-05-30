package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.core.constant.StringPool;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.data.doris.model.ColumnDefinition;
import cn.fxbin.bubble.data.doris.model.KeysType;
import cn.fxbin.bubble.data.doris.model.PartitionDefinition;
import cn.fxbin.bubble.data.doris.model.TableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JdbcDorisDdlOperations
 * Doris DDL 操作的 JDBC 实现，支持通用的 DDL 执行和更全面的表级 DDL 管理。
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
public class JdbcDorisDdlOperations implements DorisDdlOperations, DorisTableDdlOperations {

    private static final Logger log = LoggerFactory.getLogger(JdbcDorisDdlOperations.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcDorisDdlOperations(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * 执行原生 DDL 语句
     *
     * @param ddl SQL DDL语句
     */
    @Override
    public void execute(String ddl) {
        log.info("Executing DDL: {}", ddl);
        try {
            namedParameterJdbcTemplate.getJdbcTemplate().execute(ddl);
        } catch (DataAccessException e) {
            log.error("执行 DDL 失败: {}", ddl, e);
            throw new ServiceException("执行 DDL 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查表是否存在
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 如果表存在则返回 true，否则返回 false
     */
    @Override
    public boolean tableExists(@Nullable String databaseName, String tableName) {
        String sql;
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (StringUtils.hasText(databaseName)) {
            sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = :databaseName AND table_name = :tableName";
            params.addValue("databaseName", databaseName);
        } else {
            // 如果数据库名为空，则尝试在当前会话的默认数据库中查找
            sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = :tableName";
        }
        params.addValue("tableName", tableName);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 检查分区是否存在
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param partitionName 分区名称
     * @return 如果分区存在则返回 true，否则返回 false
     */
    @Override
    public boolean partitionExists(@Nullable String databaseName, String tableName, String partitionName) {
        // Doris 1.x 版本 information_schema.partitions 可能不包含 PartitionName，需要依赖 SHOW PARTITIONS
        // 但为了安全和通用性，这里优先使用 information_schema.partitions
        String sql;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (StringUtils.hasText(databaseName)) {
            sql = "SELECT COUNT(*) FROM information_schema.partitions WHERE table_schema = :databaseName AND table_name = :tableName AND partition_name = :partitionName";
            params.addValue("databaseName", databaseName);
        } else {
            sql = "SELECT COUNT(*) FROM information_schema.partitions WHERE table_name = :tableName AND partition_name = :partitionName";
        }
        params.addValue("tableName", tableName);
        params.addValue("partitionName", partitionName);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 创建分区
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param partitionDefinition 分区定义，例如: PARTITION p202301 VALUES LESS THAN ('2023-02-01')
     */
    @Override
    public void addPartition(@Nullable String databaseName, String tableName, String partitionDefinition) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String ddl = String.format("ALTER TABLE %s ADD %s", fullTableName, partitionDefinition);
        execute(ddl);
    }

    /**
     * 删除分区
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param partitionName 分区名称
     */
    @Override
    public void dropPartition(@Nullable String databaseName, String tableName, String partitionName) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String ddl = String.format("ALTER TABLE %s DROP PARTITION %s", fullTableName, partitionName);
        execute(ddl);
    }

    /**
     * 获取自动创建的分区名称，根据传入的日期值
     *
     * @param dateValue 日期值
     * @param granularity 分区粒度 (year, month, day)
     * @return 分区名称
     */
    @Override
    public String getAutoPartitionName(String dateValue, String granularity) {
        String prefix = "p"; // 分区前缀
        DateTimeFormatter targetFormatter;

        // 根据粒度选择最终输出的格式化器
        switch (granularity.toLowerCase(Locale.ROOT)) {
            case "year":
                targetFormatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            case "month":
                targetFormatter = DateTimeFormatter.ofPattern("yyyyMM");
                break;
            case "day":
                targetFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                break;
            default:
                throw new IllegalArgumentException("不支持的粒度: " + granularity);
        }

        try {
            // 尝试直接解析为 LocalDate
            LocalDate date;
            if (dateValue.matches("\\d{4}-\\d{2}-\\d{2}.*")) { // 尝试 ISO 日期格式 (e.g., "2023-01-15 10:00:00")
                date = LocalDate.parse(dateValue.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (dateValue.matches("\\d{8}")) { // 尝试 YYYYMMDD 格式 (e.g., "20230115")
                date = LocalDate.parse(dateValue, DateTimeFormatter.BASIC_ISO_DATE);
            } else if (dateValue.matches("\\d{4}")) { // 尝试 YYYY 格式 (e.g., "2023")
                date = LocalDate.parse(dateValue + "0101", DateTimeFormatter.BASIC_ISO_DATE);
            } else if (dateValue.matches("\\d{6}")) { // 尝试 YYYYMM 格式 (e.g., "202301")
                date = LocalDate.parse(dateValue + "01", DateTimeFormatter.BASIC_ISO_DATE);
            } else {
                // 如果是时间戳，尝试解析为时间戳
                date = LocalDate.ofEpochDay(Long.parseLong(dateValue) / (1000 * 60 * 60 * 24));
            }
            return prefix + targetFormatter.format(date);
        } catch (DateTimeParseException | NumberFormatException e) {
            log.error("无法解析日期值 {} 为粒度 {} 对应的日期，请检查输入格式。异常信息: {}", dateValue, granularity, e.getMessage());
            throw new IllegalArgumentException("无法解析日期值: " + dateValue + "，请确保其与粒度 " + granularity + " 兼容.", e);
        }
    }


    /**
     * 根据 TableDefinition 对象创建 Doris 表。
     *
     * @param tableDefinition 表定义对象
     */
    @Override
    public void createTable(TableDefinition tableDefinition) {
        // 构建 DDL 语句
        String ddl = generateCreateTableDDL(tableDefinition);
        execute(ddl);
    }

    /**
     * 删除 Doris 表。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     */
    @Override
    public void dropTable(@Nullable String databaseName, String tableName) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String ddl = String.format("DROP TABLE %s", fullTableName);
        execute(ddl);
    }

    /**
     * 重命名 Doris 表。
     *
     * @param databaseName 数据库名称
     * @param oldTableName 旧表名称
     * @param newTableName 新表名称
     */
    @Override
    public void renameTable(@Nullable String databaseName, String oldTableName, String newTableName) {
        String fullOldTableName = getFullTableName(databaseName, oldTableName);
        String ddl = String.format("ALTER TABLE %s RENAME %s", fullOldTableName, newTableName);
        execute(ddl);
    }

    /**
     * 修改表名。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param newTableName 新表名称
     */
    @Override
    public void alterTableName(@Nullable String databaseName, String tableName, String newTableName) {
        renameTable(databaseName, tableName, newTableName); // Doris 1.2.x 及更高版本 ALTER TABLE table RENAME TO new_table_name
    }

    /**
     * 为 Doris 表添加新列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnDefinition 新列定义
     */
    @Override
    public void addColumn(@Nullable String databaseName, String tableName, ColumnDefinition columnDefinition) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String columnDdl = buildColumnDefinition(columnDefinition);
        String ddl = String.format("ALTER TABLE %s ADD COLUMN %s", fullTableName, columnDdl);
        execute(ddl);
    }

    /**
     * 修改 Doris 表列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnDefinition 列定义
     */
    @Override
    public void modifyColumn(@Nullable String databaseName, String tableName, ColumnDefinition columnDefinition) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String columnDdl = buildColumnDefinition(columnDefinition);
        String ddl = String.format("ALTER TABLE %s MODIFY COLUMN %s", fullTableName, columnDdl);
        execute(ddl);
    }

    /**
     * 删除 Doris 表的列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnName 要删除的列名
     */
    @Override
    public void dropColumn(@Nullable String databaseName, String tableName, String columnName) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String ddl = String.format("ALTER TABLE %s DROP COLUMN %s", fullTableName, columnName);
        execute(ddl);
    }

    /**
     * 排序 Doris 表的列。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param columnName 要排序的列名
     * @param afterColumnName 排序到指定列之后，如果为 null，则排到第一个
     */
    @Override
    public void orderColumn(@Nullable String databaseName, String tableName, String columnName, @Nullable String afterColumnName) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String ddl;
        if (StringUtils.hasText(afterColumnName)) {
            ddl = String.format("ALTER TABLE %s ORDER BY %s AFTER %s", fullTableName, columnName, afterColumnName);
        } else {
            ddl = String.format("ALTER TABLE %s ORDER BY %s FIRST", fullTableName, columnName);
        }
        execute(ddl);
    }

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
    @Override
    public void createIndex(@Nullable String databaseName, String tableName, String indexName, String indexColumns, String indexType, @Nullable String comment) {
        String fullTableName = getFullTableName(databaseName, tableName);
        StringBuilder ddlBuilder = new StringBuilder();
        ddlBuilder.append(String.format("CREATE INDEX %s ON %s (%s) USING %s", indexName, fullTableName, indexColumns, indexType));
        if (StringUtils.hasText(comment)) {
            ddlBuilder.append(String.format(" COMMENT '%s'", comment));
        }
        execute(ddlBuilder.toString());
    }

    /**
     * 删除 Doris 表索引。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param indexName 索引名称
     */
    @Override
    public void dropIndex(@Nullable String databaseName, String tableName, String indexName) {
        String fullTableName = getFullTableName(databaseName, tableName);
        String ddl = String.format("DROP INDEX %s ON %s", indexName, fullTableName);
        execute(ddl);
    }

    /**
     * 启用或禁用表的 AUTO PARTITION 功能。
     * 注意：Doris 目前没有直接 ALTER TABLE 启用/禁用 AUTO PARTITION 的 DDL 语句，
     * 这个方法通常是通过修改表属性实现，或者用于前端逻辑控制。
     * 如果Doris未来支持，则可以直接使用 DDL。
     * 目前，此方法更多是作为一个概念性的功能，实际可能需要通过其他方式来实现。
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param enable 是否启用
     */
    @Override
    public void enableAutoPartition(@Nullable String databaseName, String tableName, boolean enable) {
        log.warn("Doris 目前没有直接 ALTER TABLE 启用/禁用 AUTO PARTITION 的 DDL 语句。此方法仅作为概念性接口提供。");
        // 如果 Doris 未来支持，可以添加类似 ALTER TABLE tb_name SET ('dynamic_partition.enable' = 'true')
        // 或 ALTER TABLE tb_name AUTO PARTITION (enable = true)
        // 但目前需要手动管理或通过配置控制自动分区逻辑的触发。
    }

    /**
     * 根据 TableDefinition 对象生成 CREATE TABLE DDL 语句。
     *
     * @param tableDefinition 表定义对象
     * @return 生成的 CREATE TABLE DDL 语句
     */
    private String generateCreateTableDDL(TableDefinition tableDefinition) {
        StringBuilder ddlBuilder = new StringBuilder();

        // 1. CREATE TABLE database.table_name
        String fullTableName = getFullTableName(tableDefinition.getDatabaseName(), tableDefinition.getTableName());
        ddlBuilder.append(String.format("CREATE TABLE %s (\n", fullTableName));

        // 2. Column Definitions
        if (CollectionUtils.isEmpty(tableDefinition.getColumnDefinitions())) {
            throw new IllegalArgumentException("列定义不能为空.");
        }
        String columnDefs = tableDefinition.getColumnDefinitions().stream()
                .map(this::buildColumnDefinition)
                .collect(Collectors.joining(",\n"));
        ddlBuilder.append(columnDefs);
        ddlBuilder.append("\n)\n");

        // 3. Keys Type (DUPLICATE KEY, UNIQUE KEY, AGGREGATE KEY, PRIMARY KEY)
        if (tableDefinition.getKeysType() != null) {
            ddlBuilder.append(tableDefinition.getKeysType().name());
            ddlBuilder.append(" KEY(");
            if (CollectionUtils.isEmpty(tableDefinition.getDistributedKeys())) {
                throw new IllegalArgumentException("分布式键不能为空.");
            }
            ddlBuilder.append(String.join(", ", tableDefinition.getDistributedKeys()));
            ddlBuilder.append(")\n");
        } else {
            throw new IllegalArgumentException("Key 类型不能为空.");
        }


        // 4. Engine
        if (StringUtils.hasText(tableDefinition.getEngine())) {
            ddlBuilder.append(String.format("ENGINE = %s\n", tableDefinition.getEngine()));
        } else {
            throw new IllegalArgumentException("表引擎不能为空.");
        }


        // 5. Partitioning
        PartitionDefinition partitionDef = tableDefinition.getPartitionDefinition();
        if (partitionDef != null) {
            if (partitionDef.getType() == PartitionDefinition.PartitionType.MANUAL) {
                if (CollectionUtils.isEmpty(partitionDef.getManualPartitions())) {
                    throw new IllegalArgumentException("手动分区定义不能为空.");
                }
                ddlBuilder.append("PARTITION BY ");
                if (StringUtils.hasText(partitionDef.getPartitionBy())) {
                    ddlBuilder.append(partitionDef.getPartitionBy()).append(" (\n");
                } else {
                    throw new IllegalArgumentException("分区列或表达式不能为空.");
                }

                String partitions = partitionDef.getManualPartitions().entrySet().stream()
                        .map(entry -> String.format("PARTITION %s %s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining(",\n"));
                ddlBuilder.append(partitions);
                ddlBuilder.append("\n)\n");
            } else if (partitionDef.getType() == PartitionDefinition.PartitionType.DYNAMIC) {
                PartitionDefinition.DynamicPartition dp = partitionDef.getDynamicPartition();
                if (dp == null || !dp.isEnable()) {
                    throw new IllegalArgumentException("动态分区配置不能为空或未启用.");
                }
                ddlBuilder.append("PROPERTIES (\n");
                ddlBuilder.append(String.format("    \"dynamic_partition.enable\" = \"%s\",\n", dp.isEnable()));
                ddlBuilder.append(String.format("    \"dynamic_partition.time_unit\" = \"%s\",\n", dp.getUnit()));
                ddlBuilder.append(String.format("    \"dynamic_partition.end\" = \"%d\",\n", dp.getEnd()));
                ddlBuilder.append(String.format("    \"dynamic_partition.prefix\" = \"%s\",\n", dp.getPrefix()));
                ddlBuilder.append(String.format("    \"dynamic_partition.buckets\" = \"%d\",\n", tableDefinition.getBuckets())); // 动态分区桶数通常与表桶数一致
                if (dp.getStart() != 0) {
                    ddlBuilder.append(String.format("    \"dynamic_partition.start\" = \"%d\",\n", dp.getStart()));
                }
                if (dp.getEvery() != 0) {
                    ddlBuilder.append(String.format("    \"dynamic_partition.every\" = \"%d\",\n", dp.getEvery()));
                }
                if (StringUtils.hasText(dp.getTimeZone())) {
                    ddlBuilder.append(String.format("    \"dynamic_partition.time_zone\" = \"%s\",\n", dp.getTimeZone()));
                }
                // 去除最后一个逗号并关闭 PROPERTIES
                ddlBuilder.setLength(ddlBuilder.length() - 2);
                ddlBuilder.append("\n)\n");

            } else if (partitionDef.getType() == PartitionDefinition.PartitionType.AUTO) {
                PartitionDefinition.AutoPartition ap = partitionDef.getAutoPartition();
                if (ap == null || !StringUtils.hasText(ap.getType()) || !StringUtils.hasText(ap.getExpression())) {
                    throw new IllegalArgumentException("自动分区配置不能为空或不完整.");
                }
                ddlBuilder.append(String.format("AUTO PARTITION BY %s (%s)\n", ap.getType(), ap.getExpression()));
                if (ap.isAllowPartitionColumnNullable() && "LIST".equalsIgnoreCase(ap.getType())) {
                    ddlBuilder.append(String.format("PROPERTIES (\n    \"allow_partition_column_nullable\" = \"%s\"\n)\n", ap.isAllowPartitionColumnNullable()));
                }
            }
        }


        // 6. Distribution (Bucketing)
        if (!CollectionUtils.isEmpty(tableDefinition.getDistributedBy())) {
            ddlBuilder.append("DISTRIBUTED BY HASH (");
            ddlBuilder.append(String.join(", ", tableDefinition.getDistributedBy()));
            ddlBuilder.append(")\n");
        } else {
            throw new IllegalArgumentException("分桶列不能为空.");
        }

        if (tableDefinition.getBuckets() > 0) {
            ddlBuilder.append(String.format("BUCKETS %d\n", tableDefinition.getBuckets()));
        } else {
            throw new IllegalArgumentException("分桶数量必须大于 0.");
        }


        // 7. Table Properties
        if (!CollectionUtils.isEmpty(tableDefinition.getProperties())) {
            ddlBuilder.append("PROPERTIES (\n");
            String properties = tableDefinition.getProperties().entrySet().stream()
                    .map(entry -> String.format("    \"%s\" = \"%s\"", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(",\n"));
            ddlBuilder.append(properties);
            ddlBuilder.append("\n)\n");
        }

        // 8. Table Comment
        if (StringUtils.hasText(tableDefinition.getComment())) {
            ddlBuilder.append(String.format("COMMENT '%s'\n", tableDefinition.getComment()));
        }

        return ddlBuilder.toString();
    }

    /**
     * 构建列定义字符串
     *
     * @param columnDefinition 列定义对象
     * @return 列定义字符串
     */
    private String buildColumnDefinition(ColumnDefinition columnDefinition) {
        StringBuilder columnBuilder = new StringBuilder();
        columnBuilder.append(String.format("`%s` %s", columnDefinition.getName(), columnDefinition.getType()));

        // 仅当 KeysType 为 AGGREGATE 且定义了 aggregationType 时才添加聚合函数
        // 修正：ColumnDefinition 不应直接判断 KeysType，KeysType 属于 TableDefinition 层面。
        // 但如果 ColumnDefinition 内部需要表示聚合类型，则应有一个明确的字段。
        // 此处假设 aggregationType 字段存在且仅在 AGGREGATE KEY 模型下使用。
        if (StringUtils.hasText(columnDefinition.getAggregationType())) {
            columnBuilder.append(String.format(" %s", columnDefinition.getAggregationType()));
        }

        if (!columnDefinition.isNullable()) {
            columnBuilder.append(" NOT NULL");
        }

        if (StringUtils.hasText(columnDefinition.getDefaultValue())) {
            columnBuilder.append(String.format(" DEFAULT '%s'", columnDefinition.getDefaultValue()));
        }

        if (StringUtils.hasText(columnDefinition.getComment())) {
            columnBuilder.append(String.format(" COMMENT '%s'", columnDefinition.getComment()));
        }
        return columnBuilder.toString();
    }

    /**
     * 获取完整的表名 (数据库名.表名)
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 完整的表名
     */
    private String getFullTableName(@Nullable String databaseName, String tableName) {
        if (StringUtils.hasText(databaseName)) {
            return String.format("%s.%s", databaseName, tableName);
        }
        return tableName;
    }
}