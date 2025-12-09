package cn.fxbin.bubble.data.duckdb.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * DuckDB 模板
 *
 * <p>
 * JdbcTemplate 的扩展，提供 DuckDB 特定功能，包括数据导入。
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 11:38
 */
@Slf4j
public class DuckDbTemplate extends JdbcTemplate {

    private final DuckDbIngester ingester;

    public DuckDbTemplate(DataSource dataSource) {
        super(dataSource);
        this.ingester = new DuckDbIngester(dataSource);
    }

    /**
     * 将数据追加到指定的 DuckDB 表中。
     *
     * @param tableName 目标表名。
     * @param rows      要插入的数据行列表（对象数组）。
     */
    public void append(String tableName, List<Object[]> rows) {
        ingester.append(tableName, rows);
    }

    /**
     * 将数据流追加到指定的 DuckDB 表中（流式处理）。
     *
     * @param tableName 目标表名。
     * @param iterator  要插入的数据行迭代器（对象数组）。
     */
    public void append(String tableName, java.util.Iterator<Object[]> iterator) {
        ingester.ingest(tableName, iterator);
    }

    /**
     * 将 Map 列表数据追加到指定的 DuckDB 表中。
     *
     * @param tableName 目标表名。
     * @param rows      要插入的数据行列表（Map）。
     * @param columns   列名列表（有序）。
     */
    public void appendMaps(String tableName, List<Map<String, Object>> rows, List<String> columns) {
        ingester.appendMaps(tableName, rows, columns);
    }

    /**
     * 将 Parquet 文件导入到表中。
     *
     * @param tableName 目标表名（将被创建）。
     * @param parquetPath Parquet 文件的路径。
     */
    public void importParquet(String tableName, String parquetPath) {
        validateTableName(tableName);
        String sql = String.format("CREATE TABLE %s AS SELECT * FROM read_parquet('%s')", tableName, parquetPath);
        log.info("执行 DuckDB 导入 Parquet：{}", sql);
        execute(sql);
    }

    /**
     * 将表（或查询结果）导出为 Parquet 文件。
     *
     * @param tableNameOrQuery 表名或 SELECT 查询。
     * @param outputPath Parquet 文件的输出路径。
     */
    public void exportParquet(String tableNameOrQuery, String outputPath) {
        String source = tableNameOrQuery.trim().toUpperCase().startsWith("SELECT")
                ? String.format("(%s)", tableNameOrQuery)
                : validateTableName(tableNameOrQuery);
        
        String sql = String.format("COPY %s TO '%s' (FORMAT PARQUET)", source, outputPath);
        log.info("执行 DuckDB 导出 Parquet：{}", sql);
        execute(sql);
    }

    private String validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("无效的表名：" + tableName);
        }
        return tableName;
    }

    /**
     * 附加另一个数据库文件。
     *
     * @param dbPath 数据库文件的路径。
     * @param alias 要使用的别名。
     */
    public void attachDatabase(String dbPath, String alias) {
        String sql = String.format("ATTACH '%s' AS %s", dbPath, alias);
        log.info("执行 DuckDB 附加：{}", sql);
        execute(sql);
    }
    
    /**
     * 安装并加载扩展。
     *
     * @param extensionName 扩展的名称（例如，'httpfs'、'spatial'）。
     */
    public void installAndLoadExtension(String extensionName) {
        log.info("安装 DuckDB 扩展：{}", extensionName);
        execute("INSTALL " + extensionName);
        log.info("加载 DuckDB 扩展：{}", extensionName);
        execute("LOAD " + extensionName);
    }

}