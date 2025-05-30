package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.autoconfigure.DorisProperties;
import cn.fxbin.bubble.data.doris.client.DorisHttpClient;
import cn.fxbin.bubble.data.doris.model.ColumnDefinition;
import cn.hutool.core.codec.Base64;
import com.dtflys.forest.http.ForestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * HttpDorisStreamLoadOperations
 * Implementation of {@link DorisStreamLoadOperations} using Forest HTTP client.
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/05/28
 */
@Slf4j
@RequiredArgsConstructor
public class HttpDorisStreamLoadOperations implements DorisStreamLoadOperations {

    private final DorisHttpClient dorisHttpClient;
    private final DorisProperties dorisProperties;
    private final JdbcTemplate jdbcTemplate; // For schema operations
    private final DorisTableDdlOperations dorisTableDdlOperations; // For DDL operations

    private String getBasicAuthHeader() {
        String auth = dorisProperties.getUsername() + ":" + dorisProperties.getPassword();
        return "Basic " + Base64.encode(auth.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ColumnDefinition> getTableColumns(String database, String table) {
        Assert.hasText(database, "Database name must not be empty");
        Assert.hasText(table, "Table name must not be empty");

        String sql = String.format(
                "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, COLUMN_KEY, IS_NULLABLE, COLUMN_DEFAULT " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' " +
                "ORDER BY ORDINAL_POSITION",
                database, table);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnDefinition def = new ColumnDefinition();
            def.setName(rs.getString("COLUMN_NAME"));
            def.setType(rs.getString("DATA_TYPE"));
            def.setComment(rs.getString("COLUMN_COMMENT"));
            def.setNullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
            // Note: COLUMN_KEY (PRI, UNI, MUL) and COLUMN_DEFAULT might need more complex mapping
            // For simplicity, we are not fully mapping them here.
            return def;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateAndAddColumns(String database, String table, List<ColumnDefinition> columnsToCheck) {
        Assert.hasText(database, "Database name must not be empty");
        Assert.hasText(table, "Table name must not be empty");
        Assert.notEmpty(columnsToCheck, "Columns to check must not be empty");

        List<ColumnDefinition> existingColumns = getTableColumns(database, table);
        Map<String, String> existingColumnMap = existingColumns.stream()
                .collect(Collectors.toMap(col -> col.getName().toLowerCase(), ColumnDefinition::getType));

        List<ColumnDefinition> newColumns = new ArrayList<>();
        for (ColumnDefinition column : columnsToCheck) {
            if (!existingColumnMap.containsKey(column.getName().toLowerCase())) {
                log.info("Column '{}' not found in table '{}.{}'. Adding it.", column.getName(), database, table);
                newColumns.add(column);
            }
            // Potentially add type validation here as well
        }

        if (!CollectionUtils.isEmpty(newColumns)) {
            for (ColumnDefinition newColumn : newColumns) {
                // Assuming addColumn method exists and works as expected
                // It might be safer to batch add columns if the DDL operation supports it
                dorisTableDdlOperations.addColumn(database, table, newColumn);
                log.info("Successfully added column '{}' type '{}' to table '{}.{}'", 
                         newColumn.getName(), newColumn.getType(), database, table);
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> streamLoad(String database, String table, InputStream data, String format, Map<String, String> options) {
        Assert.hasText(database, "Database name must not be empty");
        Assert.hasText(table, "Table name must not be empty");
        Assert.notNull(data, "Data input stream must not be null");
        Assert.hasText(format, "Data format must not be empty");

        String label = options.getOrDefault("label", "label_" + UUID.randomUUID().toString().replace("-", ""));
        String columns = options.getOrDefault("columns", null);
        String columnSeparator = options.getOrDefault("column_separator", format.equalsIgnoreCase("csv") ? "," : null);
        String lineDelimiter = options.getOrDefault("line_delimiter", format.equalsIgnoreCase("csv") ? "\n" : null);
        Boolean stripOuterArray = Boolean.valueOf(options.getOrDefault("strip_outer_array", "false"));

        // Remove standard options from 'otherOptions' to avoid duplication
        Map<String, String> otherStreamLoadOptions = options.entrySet().stream()
                .filter(entry -> !List.of("label", "columns", "column_separator", "line_delimiter", "strip_outer_array").contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("Executing StreamLoad to {}.{} with label: {}, format: {}, columns: {}", 
                 database, table, label, format, columns != null ? columns : "(auto)");

        ForestResponse<Map<String, Object>> response = dorisHttpClient.streamLoad(
                database,
                table,
                getBasicAuthHeader(),
                format,
                columns,
                columnSeparator,
                lineDelimiter,
                stripOuterArray,
                label,
                otherStreamLoadOptions,
                data
        );

        if (response.isSuccess()) {
            Map<String, Object> result = response.getResult();
            log.info("StreamLoad successful for label '{}'. Response: {}", label, result);
            // Example success response: {"TxnId":19027,"Label":"label_xxx","Status":"Success","Message":"stream load error","NumberTotalRows":10,"NumberLoadedRows":10,"NumberFilteredRows":0,"NumberUnselectedRows":0,"LoadBytes":40,"LoadTimeMs":67,"BeginTxnTimeMs":0,"StreamLoadPutTimeMs":1,"ReadDataTimeMs":0,"WriteDataTimeMs":10,"CommitAndPublishTimeMs":55}
            // Check Status for detailed success, e.g., "Success", "Publish Timeout"
            if (result != null && !"Success".equalsIgnoreCase(String.valueOf(result.get("Status")))) {
                log.warn("StreamLoad for label '{}' completed but status is not 'Success': {}. Full response: {}", label, result.get("Status"), result);
            }
            return result;
        } else {
            String errorMessage = String.format("StreamLoad failed for label '%s'. HTTP Status: %d. Error: %s",
                    label, response.getStatusCode(), response.getContent());
            log.error(errorMessage);
            // Consider throwing a custom exception here
            throw new RuntimeException(errorMessage);
        }
    }
}