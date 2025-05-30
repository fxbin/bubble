package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.model.ColumnDefinition;
import cn.fxbin.bubble.data.doris.model.TableDefinition;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * DorisStreamLoadOperations
 * Doris StreamLoad 操作接口，提供基于 HTTP 的数据导入能力
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/1/7 23:35
 */
public interface DorisStreamLoadOperations {

    /**
     * 获取表的列信息
     *
     * @param database 数据库名
     * @param table 表名
     * @return 列定义列表
     */
    List<ColumnDefinition> getTableColumns(String database, String table);

    /**
     * 校验并自动添加新列
     *
     * @param database 数据库名
     * @param table 表名
     * @param columns 需要校验的列定义
     * @return 是否有新列被添加
     */
    boolean validateAndAddColumns(String database, String table, List<ColumnDefinition> columns);

    /**
     * 执行 StreamLoad 导入
     *
     * @param database 数据库名
     * @param table 表名
     * @param data 数据输入流
     * @param format 数据格式（如 CSV, JSON）
     * @param options 导入选项
     * @return 导入结果
     */
    Map<String, Object> streamLoad(String database, String table, InputStream data, String format, Map<String, String> options);

    /**
     * 执行 StreamLoad 导入（使用 JSON 格式）
     *
     * @param database 数据库名
     * @param table 表名
     * @param jsonData JSON 格式的数据输入流
     * @param options 导入选项
     * @return 导入结果
     */
    default Map<String, Object> streamLoadJson(String database, String table, InputStream jsonData, Map<String, String> options) {
        return streamLoad(database, table, jsonData, "json", options);
    }

    /**
     * 执行 StreamLoad 导入（使用 CSV 格式）
     *
     * @param database 数据库名
     * @param table 表名
     * @param csvData CSV 格式的数据输入流
     * @param options 导入选项
     * @return 导入结果
     */
    default Map<String, Object> streamLoadCsv(String database, String table, InputStream csvData, Map<String, String> options) {
        return streamLoad(database, table, csvData, "csv", options);
    }
}