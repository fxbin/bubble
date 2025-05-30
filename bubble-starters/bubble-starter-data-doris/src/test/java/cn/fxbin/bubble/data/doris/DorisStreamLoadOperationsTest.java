package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.model.ColumnDefinition;
import cn.fxbin.bubble.data.doris.model.KeysType;
import cn.fxbin.bubble.data.doris.model.TableDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DorisStreamLoadOperationsTest
 * <p>
 * Integration tests for {@link DorisStreamLoadOperations} implementation.
 * Tests StreamLoad functionality including column validation and data import.
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/29 10:00
 */
public class DorisStreamLoadOperationsTest extends BaseDorisIntegrationTest {

    @Autowired
    private DorisStreamLoadOperations streamLoadOperations;

    @Autowired
    private JdbcDorisDdlOperations ddlOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_DATABASE = "test_db";
    private static final String TEST_TABLE = "test_stream_load";

    @BeforeEach
    void setUp() {
        // Create test database if not exists
        ddlOperations.execute("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
        ddlOperations.execute("USE " + TEST_DATABASE);

        // Drop test table if exists
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE);
        }

        // Create test table
        TableDefinition tableDef = new TableDefinition();
        tableDef.setDatabaseName(TEST_DATABASE);
        tableDef.setTableName(TEST_TABLE);
        tableDef.setKeysType(KeysType.UNIQUE);

        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(ColumnDefinition.builder().name("id").type("INT").nullable(false).comment("Primary key").isKey(true).build());
        columns.add(ColumnDefinition.builder().name("name").type("VARCHAR(50)").nullable(true).comment("Name").build());
        columns.add(ColumnDefinition.builder().name("age").type("INT").nullable(true).comment("Age").build());
        columns.add(ColumnDefinition.builder().name("score").type("DECIMAL(10,2)").nullable(true).comment("Score").build()); // Changed event_date to score
        tableDef.setColumnDefinitions(columns);

        tableDef.setDistributedBy(java.util.Collections.singletonList("id"));
        tableDef.setBuckets(3);

        ddlOperations.createTable(tableDef);
    }

    @Test
    void testGetTableColumns() {
        List<ColumnDefinition> columns = streamLoadOperations.getTableColumns(TEST_DATABASE, TEST_TABLE);
        assertNotNull(columns);
        assertEquals(4, columns.size());

        // Verify column details
        assertTrue(columns.stream().anyMatch(col -> "id".equals(col.getName())));
        assertTrue(columns.stream().anyMatch(col -> "name".equals(col.getName())));
        assertTrue(columns.stream().anyMatch(col -> "age".equals(col.getName())));
        assertTrue(columns.stream().anyMatch(col -> "score".equals(col.getName()))); // Assertion is now correct
    }

    @Test
    void testValidateAndAddColumns() {
        List<ColumnDefinition> newColumns = new ArrayList<>();
        newColumns.add(new ColumnDefinition("email", "VARCHAR(100)", true, "User email"));
        newColumns.add(new ColumnDefinition("created_at", "DATETIME", true, "Creation time"));

        boolean columnsAdded = streamLoadOperations.validateAndAddColumns(TEST_DATABASE, TEST_TABLE, newColumns);
        assertTrue(columnsAdded);

        // Verify new columns were added
        List<ColumnDefinition> updatedColumns = streamLoadOperations.getTableColumns(TEST_DATABASE, TEST_TABLE);
        assertEquals(6, updatedColumns.size());
        assertTrue(updatedColumns.stream().anyMatch(col -> "email".equals(col.getName())));
        assertTrue(updatedColumns.stream().anyMatch(col -> "created_at".equals(col.getName())));
    }

    @Test
    void testStreamLoadJson() {
        String jsonData = "[" +
                "{\"id\": 1, \"name\": \"Alice\", \"age\": 25, \"score\": 95.5}," +
                "{\"id\": 2, \"name\": \"Bob\", \"age\": 30, \"score\": 88.0}" +
                "]";

        Map<String, String> options = new HashMap<>();
        options.put("strip_outer_array", "true");
        options.put("format", "json");
        options.put("ignore_json_size", "true");

        Map<String, Object> result = streamLoadOperations.streamLoadJson(
                TEST_DATABASE,
                TEST_TABLE,
                new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8)),
                options
        );

        assertNotNull(result);
        assertEquals("Success", result.get("Status"));
        assertEquals(2L, result.get("NumberLoadedRows"));

        // Verify data was loaded correctly
        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TEST_DATABASE + "." + TEST_TABLE,
                Integer.class
        ));
    }

    @Test
    void testStreamLoadCsv() {
        String csvData = "id,name,age,score\n" +
                "3,Charlie,35,92.5\n" +
                "4,David,28,87.8";

        Map<String, String> options = new HashMap<>();
        options.put("column_separator", ",");
        options.put("columns", "id,name,age,score");
        options.put("format", "csv");

        Map<String, Object> result = streamLoadOperations.streamLoadCsv(
                TEST_DATABASE,
                TEST_TABLE,
                new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)),
                options
        );

        assertNotNull(result);
        assertEquals("Success", result.get("Status"));
        assertEquals(2L, result.get("NumberLoadedRows"));

        // Verify data was loaded correctly
        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TEST_DATABASE + "." + TEST_TABLE,
                Integer.class
        ));
    }

    @Test
    void testStreamLoadWithRetry() {
        // 准备一个较大的数据集，可能触发重试机制
        StringBuilder jsonData = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) jsonData.append(",");
            jsonData.append(String.format(
                    "{\"id\": %d, \"name\": \"User%d\", \"age\": %d, \"score\": %.1f}",
                    i + 1, i + 1, 20 + (i % 50), 60.0 + (i % 40)
            ));
        }
        jsonData.append("]");

        Map<String, String> options = new HashMap<>();
        options.put("strip_outer_array", "true");
        options.put("format", "json");
        options.put("ignore_json_size", "true");
        options.put("max_filter_ratio", "0.1"); // 允许最多10%的数据过滤

        Map<String, Object> result = streamLoadOperations.streamLoadJson(
                TEST_DATABASE,
                TEST_TABLE,
                new ByteArrayInputStream(jsonData.toString().getBytes(StandardCharsets.UTF_8)),
                options
        );

        assertNotNull(result);
        assertEquals("Success", result.get("Status"));
        assertTrue((Long) result.get("NumberLoadedRows") > 0);

        // 验证数据是否正确加载
        assertTrue(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TEST_DATABASE + "." + TEST_TABLE,
                Integer.class
        ) > 0);
    }
}