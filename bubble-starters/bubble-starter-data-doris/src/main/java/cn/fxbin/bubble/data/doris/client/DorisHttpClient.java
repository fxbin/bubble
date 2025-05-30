package cn.fxbin.bubble.data.doris.client;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;

import java.io.InputStream;
import java.util.Map;

/**
 * DorisHttpClient
 * Forest client for interacting with Doris FE HTTP API.
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/05/28
 */
@BaseRequest(
        baseURL = "http://${feHost}:${feHttpPort}" // 从配置中读取 FE 地址和 HTTP 端口
)
public interface DorisHttpClient {

    /**
     * Stream load data to Doris.
     *
     * @param database the database name
     * @param table the table name
     * @param user the Doris user
     * @param password the Doris password
     * @param format data format (e.g., csv, json)
     * @param columns column mapping, e.g., "c1,c2,c3"
     * @param columnSeparator column separator for CSV format
     * @param lineDelimiter line delimiter for CSV format
     * @param stripOuterArray strip outer array for JSON format
     * @param label stream load label
     * @param otherOptions other stream load options
     * @param data the input stream of data
     * @return ForestResponse containing the load result
     */
    @Put(url = "/api/${database}/${table}/_stream_load")
    @Body("{data}")
    ForestResponse<Map<String, Object>> streamLoad(
            @Var("database") String database,
            @Var("table") String table,
            @Header("Authorization") String basicAuth, // Basic Auth: "Basic " + Base64.encode(user + ":" + password)
            @Header("format") String format, // e.g., "json", "csv"
            @Header("columns") String columns, // Optional, e.g., "c1,c2,c3"
            @Header("column_separator") String columnSeparator, // Optional, for CSV
            @Header("line_delimiter") String lineDelimiter, // Optional, for CSV
            @Header("strip_outer_array") Boolean stripOuterArray, // Optional, for JSON, default false
            @Header("label") String label, // Optional, unique load label
            @Header Map<String, String> otherOptions, // Other valid StreamLoad headers
            InputStream data
    );

    /**
     * Execute SQL against Doris.
     *
     * @param database the database name
     * @param sql the SQL to execute
     * @param user the Doris user
     * @param password the Doris password
     * @return ForestResponse containing the query result
     */
    @Post(url = "/api/query/sql")
    @Body("{\"stmt\": \"${sql}\"}") // JSON body for SQL
    ForestResponse<Map<String, Object>> executeSql(
            @Header("Authorization") String basicAuth, // Basic Auth
            @Header("db") String database, // Specify database in header
            @Var("sql") String sql
    );

}