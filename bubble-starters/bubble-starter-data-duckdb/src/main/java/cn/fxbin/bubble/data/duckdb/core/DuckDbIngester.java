package cn.fxbin.bubble.data.duckdb.core;

import cn.fxbin.bubble.data.duckdb.core.handler.TypeHandler;
import cn.fxbin.bubble.data.duckdb.core.handler.TypeHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.duckdb.DuckDBAppender;
import org.duckdb.DuckDBConnection;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DuckDB 数据摄取器
 *
 * <p>
 * 使用 DuckDB Appender 的高性能数据摄取工具。
 * 适用于将大型数据集（例如从 IoTDB）传输到 DuckDB。
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 15:05
 */
@Slf4j
public class DuckDbIngester {

    private final DataSource dataSource;

    public DuckDbIngester(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 将数据列表追加到指定的 DuckDB 表中。
     *
     * @param tableName 目标表名。
     * @param rows      要插入的数据行列表（对象数组）。
     */
    public void append(String tableName, List<Object[]> rows) {
        ingest(tableName, rows.iterator());
    }

    /**
     * 将 Map 列表数据追加到指定的 DuckDB 表中。
     *
     * @param tableName 目标表名。
     * @param rows      要插入的数据行列表（Map）。
     * @param columns   列名列表（有序），必须与数据库表列顺序一致。
     */
    public void appendMaps(String tableName, List<Map<String, Object>> rows, List<String> columns) {
        Iterator<Object[]> iterator = new Iterator<>() {
            private final Iterator<Map<String, Object>> mapIterator = rows.iterator();

            @Override
            public boolean hasNext() {
                return mapIterator.hasNext();
            }

            @Override
            public Object[] next() {
                Map<String, Object> map = mapIterator.next();
                Object[] row = new Object[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    row[i] = map.get(columns.get(i));
                }
                return row;
            }
        };
        ingest(tableName, iterator);
    }

    /**
     * 使用 DuckDB Appender 摄取数据。
     *
     * @param tableName    目标表名。
     * @param dataIterator 数据行迭代器（对象数组）。
     */
    public void ingest(String tableName, Iterator<Object[]> dataIterator) {
        ingest(tableName, dataIterator, 10000); // 默认批次日志大小
    }

    /**
     * 使用 DuckDB Appender 摄取数据，并记录进度。
     *
     * @param tableName    目标表名。
     * @param dataIterator 数据行迭代器。
     * @param logInterval  每隔 N 行记录一次进度。
     */
    public void ingest(String tableName, Iterator<Object[]> dataIterator, int logInterval) {
        try (Connection conn = dataSource.getConnection()) {
            
            // 1. 获取列元数据并预计算 TypeHandlers
            int[] columnTypes = getColumnTypes(conn, tableName);
            TypeHandler[] handlers = new TypeHandler[columnTypes.length];
            for (int i = 0; i < columnTypes.length; i++) {
                handlers[i] = TypeHandlerFactory.getHandler(columnTypes[i]);
            }
            
            // 2. 解包以获取原生的 DuckDBConnection
            DuckDBConnection duckDBConn = conn.unwrap(DuckDBConnection.class);
            
            try (DuckDBAppender appender = duckDBConn.createAppender(DuckDBConnection.DEFAULT_SCHEMA, tableName)) {
                long count = 0;
                while (dataIterator.hasNext()) {
                    Object[] row = dataIterator.next();
                    appender.beginRow();
                    
                    if (row.length != handlers.length) {
                         throw new SQLException("数据行长度 (" + row.length + ") 与表列数 (" + handlers.length + ") 不匹配");
                    }

                    for (int i = 0; i < row.length; i++) {
                        // 使用策略模式：循环中不再进行 instanceof 检查
                        handlers[i].append(appender, row[i]);
                    }
                    appender.endRow();
                    
                    count++;
                    if (count % logInterval == 0) {
                        log.info("已摄取 {} 行到 {}", count, tableName);
                    }
                }
                log.info("摄取完成。总行数：{}", count);
            }
        } catch (SQLException e) {
            throw new RuntimeException("摄取数据到 DuckDB 失败", e);
        }
    }

    private int[] getColumnTypes(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 0")) {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            int[] types = new int[count];
            for (int i = 0; i < count; i++) {
                types[i] = meta.getColumnType(i + 1);
            }
            return types;
        }
    }
}