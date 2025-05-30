package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.model.ColumnDefinition;
import cn.fxbin.bubble.data.doris.model.KeysType;
import cn.fxbin.bubble.data.doris.model.PartitionDefinition;
import cn.fxbin.bubble.data.doris.model.TableDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DorisDdlOperationsTest
 * <p>
 * Integration tests for {@link DorisTableDdlOperations} and {@link DorisDdlOperations}.
 * Tests DDL operations like table creation, modification, partitioning, etc.
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/29 10:00
 */
public class DorisDdlOperationsTest extends BaseDorisIntegrationTest {

    @Autowired
    private JdbcDorisDdlOperations ddlOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_DATABASE = "test_db";
    private static final String TEST_TABLE_DDL = "test_ddl_operations";
    private static final String TEST_TABLE_PARTITION = "test_partition_operations";

    @BeforeEach
    void setUp() {
        ddlOperations.execute("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
        ddlOperations.execute("USE " + TEST_DATABASE);

        // Clean up tables from previous runs
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_DDL)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE_DDL);
        }
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_PARTITION)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE_PARTITION);
        }
    }

    private TableDefinition createBasicTableDefinition(String tableName) {
        TableDefinition tableDef = new TableDefinition();
        tableDef.setDatabaseName(TEST_DATABASE);
        tableDef.setTableName(tableName);
        tableDef.setKeysType(KeysType.UNIQUE);

        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(ColumnDefinition.builder().name("id").type("INT").nullable(false).comment("Primary key").isKey(true).build());
        columns.add(ColumnDefinition.builder().name("event_date").type("DATE").nullable(false).comment("Event date").build());
        columns.add(ColumnDefinition.builder().name("value").type("VARCHAR(255)").nullable(true).comment("Some value").build());
        tableDef.setColumnDefinitions(columns);

        tableDef.setDistributedBy(Collections.singletonList("id"));
        tableDef.setBuckets(3);
        return tableDef;
    }

    @Test
    void testCreateTableAndTableExists() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_DDL);
        ddlOperations.createTable(tableDef);
        assertTrue(ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_DDL), "Table should exist after creation");
    }

    @Test
    void testDropTable() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_DDL);
        ddlOperations.createTable(tableDef);
        assertTrue(ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_DDL), "Table should exist before drop");
        ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE_DDL);
        assertFalse(ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_DDL), "Table should not exist after drop");
    }

    @Test
    void testAddAndDropColumn() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_DDL);
        ddlOperations.createTable(tableDef);

        ColumnDefinition newColumn = new ColumnDefinition("extra_info", "TEXT", true, "Extra information");
        ddlOperations.addColumn(TEST_DATABASE, TEST_TABLE_DDL, newColumn);

        // Verify column exists (using information_schema or describe table)
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                String.format("DESCRIBE %s.%s", TEST_DATABASE, TEST_TABLE_DDL)
        );
        assertTrue(columns.stream().anyMatch(col -> "extra_info".equalsIgnoreCase((String) col.get("Field"))),
                   "New column should be added");

        ddlOperations.dropColumn(TEST_DATABASE, TEST_TABLE_DDL, "extra_info");
        columns = jdbcTemplate.queryForList(
                String.format("DESCRIBE %s.%s", TEST_DATABASE, TEST_TABLE_DDL)
        );
        assertFalse(columns.stream().anyMatch(col -> "extra_info".equalsIgnoreCase((String) col.get("Field"))),
                    "Column should be dropped");
    }

    @Test
    void testModifyColumn() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_DDL);
        ddlOperations.createTable(tableDef);

        ColumnDefinition modifiedColumn = new ColumnDefinition("value", "TEXT", true, "Modified value column");
        ddlOperations.modifyColumn(TEST_DATABASE, TEST_TABLE_DDL, modifiedColumn);

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                String.format("DESCRIBE %s.%s", TEST_DATABASE, TEST_TABLE_DDL)
        );
        columns.stream()
                .filter(col -> "value".equalsIgnoreCase((String) col.get("Field")))
                .findFirst()
                .ifPresentOrElse(
                        col -> assertTrue(((String) col.get("Type")).toLowerCase().startsWith("text"), "Column type should be modified to TEXT"),
                        () -> fail("Column 'value' not found")
                );
    }

    @Test
    void testCreateManualPartitionTableAndAddPartition() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_PARTITION);
        tableDef.setPartitionDefinition(PartitionDefinition.builder()
                .type(PartitionDefinition.PartitionType.MANUAL)
                .partitionBy("event_date")
                .manualPartitions(Map.of(
                        "p202301", "VALUES LESS THAN ('2023-02-01')",
                        "p202302", "VALUES LESS THAN ('2023-03-01')"
                ))
                .build());

        ddlOperations.createTable(tableDef);
        assertTrue(ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_PARTITION), "Partitioned table should exist");
        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_PARTITION, "p202301"), "Partition p202301 should exist");

        String newPartitionDdl = "PARTITION p202303 VALUES LESS THAN ('2023-04-01')";
        ddlOperations.addPartition(TEST_DATABASE, TEST_TABLE_PARTITION, newPartitionDdl);
        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_PARTITION, "p202303"), "Partition p202303 should be added");
    }

    @Test
    void testDropPartition() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_PARTITION);
        tableDef.setPartitionDefinition(PartitionDefinition.builder()
                .type(PartitionDefinition.PartitionType.MANUAL)
                .partitionBy("event_date")
                .manualPartitions(Map.of("p202401", "VALUES LESS THAN ('2024-02-01')"))
                .build());
        ddlOperations.createTable(tableDef);

        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_PARTITION, "p202401"), "Partition should exist before drop");
        ddlOperations.dropPartition(TEST_DATABASE, TEST_TABLE_PARTITION, "p202401");
        assertFalse(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_PARTITION, "p202401"), "Partition should not exist after drop");
    }

    @Test
    void testCreateAutoPartitionTable() {
        TableDefinition tableDef = createBasicTableDefinition(TEST_TABLE_PARTITION + "_auto");
        tableDef.setPartitionDefinition(PartitionDefinition.builder()
                .type(PartitionDefinition.PartitionType.AUTO)
                .partitionBy("event_date") // This would typically be a date_trunc expression for AUTO PARTITION in Doris
                .autoPartition(PartitionDefinition.AutoPartition.builder()
                        .type("RANGE") // Assuming RANGE for date based
                        .expression("date_trunc(event_date, 'MONTH')") // Example expression
                        .build())
                .build());
        // Properties for auto partition are set in tableDef.setProperties()
        tableDef.setProperties(Map.of(
                "dynamic_partition.enable", "true",
                "dynamic_partition.time_unit", "MONTH",
                "dynamic_partition.time_zone", "Asia/Shanghai",
                "dynamic_partition.start", "-2147483648", // effectively no past limit
                "dynamic_partition.end", "3", // create partitions for next 3 months
                "dynamic_partition.prefix", "p",
                "dynamic_partition.buckets", "3"
        ));

        ddlOperations.createTable(tableDef);
        assertTrue(ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_PARTITION + "_auto"), "Auto-partitioned table should exist");

        // Verify auto partition properties (might require specific SHOW CREATE TABLE and parsing)
        String createTableStmt = jdbcTemplate.queryForObject(
                String.format("SHOW CREATE TABLE %s.%s", TEST_DATABASE, TEST_TABLE_PARTITION + "_auto"),
                (rs, rowNum) -> rs.getString("Create Table")
        );
        assertNotNull(createTableStmt);
        assertTrue(createTableStmt.contains("PARTITION BY RANGE(`event_date`)"), "Should be partitioned by event_date");
        assertTrue(createTableStmt.contains("'partition_ttl_number' = '3'"), "Should have partition_ttl_number property");
        assertTrue(createTableStmt.contains("'auto_partition_type' = 'MONTH'"), "Should have auto_partition_type property");
    }

}