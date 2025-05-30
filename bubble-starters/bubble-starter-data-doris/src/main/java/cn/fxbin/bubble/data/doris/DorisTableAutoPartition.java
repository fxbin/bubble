package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.core.constant.StringPool;
import cn.fxbin.bubble.data.doris.autoconfigure.DorisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DorisTableAutoPartition
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
public class DorisTableAutoPartition {

    private static final Logger log = LoggerFactory.getLogger(DorisTableAutoPartition.class);

    private final DorisDdlOperations ddlOperations;
    private final DorisProperties.AutoPartition autoPartitionProperties;

    // 正则表达式用于从 DDL 语句中提取表名和数据库名
    // 例如：CREATE TABLE date_table AUTO PARTITION BY RANGE (date_trunc(TIME_STAMP, 'month')) ()
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:(?<database>[a-zA-Z_][a-zA-Z0-9_]*)\\.)?(?<tableName>[a-zA-Z_][a-zA-Z0-9_]*)"
                    + "\\s+AUTO\\s+PARTITION\\s+BY\\s+(?<partitionType>RANGE|LIST)\\s*\\((?<partitionBy>[^)]+)\\)",
            Pattern.CASE_INSENSITIVE
    );

    // 正则表达式用于提取 date_trunc 函数的粒度
    private static final Pattern DATE_TRUNC_PATTERN = Pattern.compile(
            "date_trunc\\([^,]+,\\s*'(?<granularity>year|month|day|hour|minute)'\\)",
            Pattern.CASE_INSENSITIVE
    );

    public DorisTableAutoPartition(DorisDdlOperations ddlOperations, DorisProperties dorisProperties) {
        this.ddlOperations = ddlOperations;
        this.autoPartitionProperties = dorisProperties.getAutoPartition();
    }

    /**
     * 检查并自动创建分区
     * <p>
     * 假设数据导入时，可以获取到分区相关的值 (例如时间戳或列表值)
     * 在实际应用中，此方法可能需要在数据导入前被调用，或者在拦截数据导入请求时调用。
     * 并且需要根据实际的 DDL 来获取表名、数据库名、分区类型、分区字段或表达式、分区粒度。
     * </p>
     *
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param ddl 创建表的DDL语句，用于解析自动分区配置
     * @param partitionValue 当前要写入数据的分区值 (时间值或列表值)
     */
    public void checkAndCreatePartition(String databaseName, String tableName, String ddl, String partitionValue) {
        if (!autoPartitionProperties.isEnabled()) {
            log.debug("Doris 自动分区功能未启用，跳过分区检查和创建。");
            return;
        }

        Matcher matcher = CREATE_TABLE_PATTERN.matcher(ddl);
        if (!matcher.find()) {
            log.warn("无法从 DDL 语句中解析出自动分区信息：{}", ddl);
            return;
        }

        // 从 DDL 中获取数据库名、表名、分区类型和分区表达式
        String parsedDatabaseName = matcher.group("database");
        String parsedTableName = matcher.group("tableName");
        String partitionTypeStr = matcher.group("partitionType");
        String partitionBy = matcher.group("partitionBy");

        // 优先使用传入的数据库名和表名，如果 DDL 中没有指定，则使用传入的
        String effectiveDatabaseName = StringUtils.hasText(parsedDatabaseName) ? parsedDatabaseName : databaseName;
        String effectiveTableName = StringUtils.hasText(parsedTableName) ? parsedTableName : tableName;

        Assert.hasText(effectiveDatabaseName, "数据库名称不能为空");
        Assert.hasText(effectiveTableName, "表名称不能为空");

        DorisProperties.AutoPartitionType autoPartitionType = DorisProperties.AutoPartitionType.valueOf(partitionTypeStr.toUpperCase(Locale.ROOT));

        String partitionName = null;
        String partitionDefinition = null;

        switch (autoPartitionType) {
            case RANGE:
                Matcher dateTruncMatcher = DATE_TRUNC_PATTERN.matcher(partitionBy);
                if (!dateTruncMatcher.find()) {
                    log.error("RANGE 自动分区类型必须使用 date_trunc 函数，且指定粒度。DDL: {}", ddl);
                    return;
                }
                String granularity = dateTruncMatcher.group("granularity");
                // 假设 partitionValue 是一个符合粒度的日期字符串，例如 "2023-01-15"
                partitionName = ddlOperations.getAutoPartitionName(partitionValue, granularity);
                // 获取下一个分区的值，例如对于 'month'，'2023-01-15' 对应分区 'p202301'，下一个分区应为 '2023-02-01'
                String nextPartitionValue = calculateNextRangePartitionValue(partitionValue, granularity);
                partitionDefinition = String.format("PARTITION %s VALUES LESS THAN ('%s')", partitionName, nextPartitionValue);
                break;
            case LIST:
                // LIST 类型直接使用 partitionValue 作为分区键
                // Doris 的 LIST 分区 VALUES IN 语法
                partitionName = "p_" + partitionValue.replaceAll("[^a-zA-Z0-9_]", "_"); // 简单处理分区名，避免特殊字符
                partitionDefinition = String.format("PARTITION %s VALUES IN ('%s')", partitionName, partitionValue);

                if (partitionBy.contains(StringPool.COMMA)) {
                    // 支持多列 LIST 分区
                    String[] partitionColumns = partitionBy.split(StringPool.COMMA);
                    String[] partitionValues = partitionValue.split(StringPool.COMMA);
                    if (partitionColumns.length != partitionValues.length) {
                        log.error("LIST 自动分区多列分区时，分区列数与分区值数不匹配. 列: {}, 值: {}", partitionBy, partitionValue);
                        return;
                    }
                    // 构建多列分区值，例如: (value1, value2)
                    partitionDefinition = String.format("PARTITION %s VALUES IN ((%s))", partitionName, String.join(StringPool.COMMA, partitionValues));
                }
                break;
            default:
                log.error("不支持的自动分区类型: {}", autoPartitionType);
                return;
        }

        if (partitionName == null || partitionDefinition == null) {
            log.error("无法构建分区名称或分区定义，跳过自动分区创建。");
            return;
        }

        // 检查分区是否存在，如果不存在则创建
        if (!ddlOperations.partitionExists(effectiveDatabaseName, effectiveTableName, partitionName)) {
            log.info("分区 {} 不存在于表 {}.{} 中，正在自动创建 ...", partitionName, effectiveDatabaseName, effectiveTableName);
            ddlOperations.addPartition(effectiveDatabaseName, effectiveTableName, partitionDefinition);
            log.info("分区 {} 已成功创建在表 {}.{} 中。", partitionName, effectiveDatabaseName, effectiveTableName);
        } else {
            log.debug("分区 {} 已存在于表 {}.{} 中，无需重复创建。", partitionName, effectiveDatabaseName, effectiveTableName);
        }
    }

    /**
     * 计算 RANGE 分区的下一个边界值
     *
     * @param currentValue 当前分区值，例如 "2023-01-15"
     * @param granularity 分区粒度 (year, month, day, hour, minute)
     * @return 下一个分区的边界值，例如 "2023-02-01"
     */
    private String calculateNextRangePartitionValue(String currentValue, String granularity) {
        LocalDateTime dateTime;
        DateTimeFormatter formatter;

        // 根据粒度解析当前值
        switch (granularity.toLowerCase(Locale.ROOT)) {
            case "year":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                dateTime = LocalDateTime.parse(currentValue + "-01-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                break;
            case "month":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                dateTime = LocalDateTime.parse(currentValue + "-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                break;
            case "day":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                dateTime = LocalDateTime.parse(currentValue + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                break;
            case "hour":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                dateTime = LocalDateTime.parse(currentValue + ":00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                break;
            case "minute":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                dateTime = LocalDateTime.parse(currentValue + ":00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                break;
            default:
                throw new IllegalArgumentException("不支持的粒度: " + granularity);
        }

        // 计算下一个日期时间
        LocalDateTime nextDateTime;
        switch (granularity.toLowerCase(Locale.ROOT)) {
            case "year":
                nextDateTime = dateTime.plusYears(1);
                formatter = DateTimeFormatter.ofPattern("yyyy-01-01"); // Doris RANGE 分区通常需要完整日期
                break;
            case "month":
                nextDateTime = dateTime.plusMonths(1);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-01"); // Doris RANGE 分区通常需要完整日期
                break;
            case "day":
                nextDateTime = dateTime.plusDays(1);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "hour":
                nextDateTime = dateTime.plusHours(1);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
                break;
            case "minute":
                nextDateTime = dateTime.plusMinutes(1);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
                break;
            default:
                throw new IllegalArgumentException("不支持的粒度: " + granularity);
        }

        return nextDateTime.format(formatter);
    }
}