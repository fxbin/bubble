package cn.fxbin.bubble.data.duckdb;

import cn.fxbin.bubble.data.duckdb.core.DuckDbIngester;
import cn.fxbin.bubble.data.duckdb.core.DuckDbManager;
import cn.fxbin.bubble.data.duckdb.core.DuckDbTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DuckDB 统一客户端入口
 *
 * <p>
 * 提供对默认 DuckDB 实例的直接访问，以及对动态 DuckDB 文件的管理。
 * 采用了外观模式（Facade Pattern），将 DuckDbTemplate、DuckDbIngester 和 DuckDbManager 的功能统一暴露。
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 16:30
 */
@Slf4j
public class DuckDbOperations {

    private final DuckDbTemplate defaultTemplate;
    private final DuckDbManager manager;
    private final DuckDbIngester defaultIngester;

    public DuckDbOperations(DuckDbTemplate defaultTemplate, DuckDbManager manager, DuckDbIngester defaultIngester) {
        Assert.notNull(defaultTemplate, "Default DuckDbTemplate must not be null");
        Assert.notNull(manager, "DuckDbManager must not be null");
        Assert.notNull(defaultIngester, "Default DuckDbIngester must not be null");
        this.defaultTemplate = defaultTemplate;
        this.manager = manager;
        this.defaultIngester = defaultIngester;
    }

    // =================================================================================================================
    // 基础查询操作 (Delegate to defaultTemplate)
    // =================================================================================================================

    /**
     * 获取默认的 DuckDbTemplate。
     *
     * @return 默认模板实例。
     */
    public DuckDbTemplate template() {
        return defaultTemplate;
    }

    /**
     * 在默认数据库上执行 SQL 查询。
     *
     * @param sql SQL 查询语句。
     * @return 结果列表（Map）。
     */
    public List<Map<String, Object>> query(String sql) {
        return defaultTemplate.queryForList(sql);
    }

    /**
     * 在默认数据库上执行 SQL 查询，并返回指定类型的对象列表。
     *
     * @param sql  SQL 查询语句。
     * @param type 结果类型。
     * @param <T>  泛型类型。
     * @return 对象列表。
     */
    public <T> List<T> query(String sql, Class<T> type) {
        return defaultTemplate.queryForList(sql, type);
    }

    /**
     * 在默认数据库上执行 SQL 查询，并返回单个对象。
     *
     * @param sql  SQL 查询语句。
     * @param type 结果类型。
     * @param <T>  泛型类型。
     * @return 单个对象，如果未找到则为 null。
     */
    public <T> T queryForObject(String sql, Class<T> type) {
        try {
            return defaultTemplate.queryForObject(sql, type);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 获取表行数统计。
     *
     * @param tableName 表名。
     * @return 行数。
     */
    public Long count(String tableName) {
        String sql = "SELECT count(*) FROM " + tableName;
        return defaultTemplate.queryForObject(sql, Long.class);
    }

    /**
     * 在默认数据库上执行 SQL 语句（DDL/DML）。
     *
     * @param sql SQL 语句。
     */
    public void execute(String sql) {
        defaultTemplate.execute(sql);
    }

    // =================================================================================================================
    // 数据导入导出 (Ingestion & Parquet)
    // =================================================================================================================

    /**
     * 向默认数据库追加数据（使用 Appender，高性能）。
     *
     * @param tableName    表名。
     * @param dataIterator 数据迭代器。
     */
    public void ingest(String tableName, Iterator<Object[]> dataIterator) {
        defaultIngester.ingest(tableName, dataIterator);
    }

    /**
     * 向默认数据库追加数据（列表方式）。
     *
     * @param tableName 表名。
     * @param rows      数据行。
     */
    public void append(String tableName, List<Object[]> rows) {
        defaultTemplate.append(tableName, rows);
    }

    /**
     * 将 Parquet 文件导入到表中。
     *
     * @param tableName   目标表名（将被创建）。
     * @param parquetPath Parquet 文件的路径。
     */
    public void importParquet(String tableName, String parquetPath) {
        defaultTemplate.importParquet(tableName, parquetPath);
    }

    /**
     * 将表（或查询结果）导出为 Parquet 文件。
     *
     * @param tableNameOrQuery 表名或 SELECT 查询。
     * @param outputPath       Parquet 文件的输出路径。
     */
    public void exportParquet(String tableNameOrQuery, String outputPath) {
        defaultTemplate.exportParquet(tableNameOrQuery, outputPath);
    }

    // =================================================================================================================
    // 动态实例操作 (Delegate to manager)
    // =================================================================================================================

    /**
     * 连接到指定的 DuckDB 文件。
     *
     * @param filePath 数据库文件路径。
     * @return 该文件的 DuckDbTemplate 实例。
     */
    public DuckDbTemplate connect(String filePath) {
        return manager.getTemplate(filePath);
    }

    /**
     * 以指定模式连接到指定的 DuckDB 文件。
     *
     * @param filePath 数据库文件路径。
     * @param readOnly 是否只读。
     * @return 该文件的 DuckDbTemplate 实例。
     */
    public DuckDbTemplate connect(String filePath, boolean readOnly) {
        return manager.getTemplate(filePath, readOnly);
    }

    /**
     * 关闭指定文件的连接。
     *
     * @param filePath 数据库文件路径。
     */
    public void close(String filePath) {
        manager.close(filePath, false);
        manager.close(filePath, true); // 尝试关闭只读连接
    }

    /**
     * 获取底层的 DuckDbManager。
     *
     * @return 管理器实例。
     */
    public DuckDbManager manager() {
        return manager;
    }
}
