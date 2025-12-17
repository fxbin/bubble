package cn.fxbin.bubble.data.duckdb.core.handler;

import org.duckdb.DuckDBAppender;

import java.sql.SQLException;

/**
 * 类型处理器接口
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 15:07
 */
public interface TypeHandler {

    /**
     * 将值附加到 DuckDB Appender
     *
     * @param appender DuckDB Appender
     * @param value    要附加的值
     * @throws SQLException 如果发生错误
     */
    void append(DuckDBAppender appender, Object value) throws SQLException;

}