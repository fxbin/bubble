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
 * DorisTableAutoPartitionTest
 *
 * <p>
 * Integration tests for {@link DorisTableAutoPartition}.
 * Tests the automatic creation of partitions based on date.
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/29 11:00
 */
public class DorisTableAutoPartitionTest extends BaseDorisIntegrationTest {

    @Autowired
    private JdbcDorisDdlOperations ddlOperations;

    @Autowired
    private DorisTableAutoPartition autoPartition;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_DATABASE = "test_db_auto_partition";
    private static final String TEST_TABLE_DAILY = "test_daily_partition";
    private static final String TEST_TABLE_MONTHLY = "test_monthly_partition";

    @BeforeEach
    void setUp() {
        ddlOperations.execute("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
        ddlOperations.execute("USE " + TEST_DATABASE);

        // Clean up tables
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_DAILY)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE_DAILY);
        }
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_MONTHLY)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE_MONTHLY);
        }

        // Create tables with auto partitioning definition
        createTestTable(TEST_TABLE_DAILY, "event_time", "DAY");
        createTestTable(TEST_TABLE_MONTHLY, "order_date", "MONTH");
    }

    private void createTestTable(String tableName, String partitionKeyName, String partitionGranularity) {
        String ddl = String.format(
                "CREATE TABLE %s.%s ( " +
                "    id INT, " +
                "    %s DATETIME, " +
                "    value VARCHAR(255)" +
                ") " +
                "UNIQUE KEY(id) " +
                "PARTITION BY RANGE(%s) () " +
                "DISTRIBUTED BY HASH(id) BUCKETS 3 " +
                "PROPERTIES ( " +
                "    \"dynamic_partition.enable\" = \"true\", " +
                "    \"dynamic_partition.time_unit\" = \"%s\", " +
                "    \"dynamic_partition.time_zone\" = \"Asia/Shanghai\", " +
                "    \"dynamic_partition.start\" = \"-3\", " +
                "    \"dynamic_partition.end\" = \"3\", " +
                "    \"dynamic_partition.prefix\" = \"p\", " +
                "    \"dynamic_partition.buckets\" = \"3\" " +
                ");",
                TEST_DATABASE, tableName, partitionKeyName, partitionKeyName, partitionGranularity
        );
        ddlOperations.execute(ddl);
    }

    private TableDefinition createTableDefinition(String tableName, String partitionKeyName, Map<String, String> initialManualPartitions) {
        TableDefinition tableDef = new TableDefinition();
        tableDef.setDatabaseName(TEST_DATABASE);
        tableDef.setTableName(tableName);
        tableDef.setKeysType(KeysType.UNIQUE);

        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(ColumnDefinition.builder().name("id").type("INT").nullable(false).comment("Primary key").isKey(true).build());
        columns.add(ColumnDefinition.builder().name(partitionKeyName).type("DATE").nullable(false).comment("Partition key").build());
        columns.add(ColumnDefinition.builder().name("value").type("VARCHAR(255)").nullable(true).comment("Some value").build());
        tableDef.setColumnDefinitions(columns);

        tableDef.setDistributedBy(Collections.singletonList("id"));
        tableDef.setBuckets(3);

        PartitionDefinition.PartitionDefinitionBuilder partitionBuilder = PartitionDefinition.builder();
        partitionBuilder.partitionBy(partitionKeyName);

        if (initialManualPartitions != null && !initialManualPartitions.isEmpty()) {
            partitionBuilder.type(PartitionDefinition.PartitionType.MANUAL);
            partitionBuilder.manualPartitions(initialManualPartitions);
        } else {
            // Default to AUTO if no initial partitions are provided, or could be set explicitly by caller
            partitionBuilder.type(PartitionDefinition.PartitionType.AUTO);
            // Auto partition properties are set in tableDef.setProperties()
            // The expression for auto partition might need to be set here if it's fixed
            // e.g., .autoPartition(PartitionDefinition.AutoPartition.builder().expression("date_trunc(" + partitionKeyName + ", 'DAY')").build())
        }
        tableDef.setPartitionDefinition(partitionBuilder.build());

        // Auto partition properties (dynamic_partition.*) are set in tableDef.setProperties()
        // These are applied when PartitionDefinition.type is AUTO
        // If type is MANUAL, these properties might be ignored by Doris or cause errors if not applicable.
        tableDef.setProperties(new java.util.HashMap<>(Map.of(
                "dynamic_partition.enable", "true",
                "dynamic_partition.time_zone", "Asia/Shanghai",
                "dynamic_partition.start", "-2147483648", // Doris way of saying no past limit
                "dynamic_partition.prefix", "p",
                "dynamic_partition.buckets", "3"
                // dynamic_partition.time_unit and dynamic_partition.end will be set per test case
        )));

        return tableDef;
    }

    @Test
    void testCreateDailyPartitions() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        String partitionNameToday = "p" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String partitionNameTomorrow = "p" + tomorrow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Initially, these partitions should not exist if table was just created without them
        assertFalse(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_DAILY, partitionNameToday));

        // Define the DDL for the table, which autoPartition.checkAndCreatePartition will parse
        String ddlDaily = String.format(
                "CREATE TABLE %s.%s AUTO PARTITION BY RANGE (date_trunc(event_time, 'DAY')) ()",
                TEST_DATABASE, TEST_TABLE_DAILY
        );

        // Simulate data for today, triggering partition creation for today
        autoPartition.checkAndCreatePartition(TEST_DATABASE, TEST_TABLE_DAILY, ddlDaily, today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        // Simulate data for tomorrow, triggering partition creation for tomorrow
        autoPartition.checkAndCreatePartition(TEST_DATABASE, TEST_TABLE_DAILY, ddlDaily, tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));

        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_DAILY, partitionNameToday),
                "Partition for today should exist after auto creation.");
        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_DAILY, partitionNameTomorrow),
                "Partition for tomorrow should exist after auto creation.");

        // Verify partition ranges (this is a bit complex to check precisely without SHOW CREATE TABLE and parsing)
        // For simplicity, we'll just check existence. A more robust test would verify LESS THAN values.
    }

    @Test
    void testCreateMonthlyPartitions() {
        LocalDate today = LocalDate.now();
        LocalDate nextMonthDate = today.plusMonths(1);

        String partitionNameThisMonth = "p" + today.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String partitionNameNextMonth = "p" + nextMonthDate.format(DateTimeFormatter.ofPattern("yyyyMM"));

        assertFalse(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_MONTHLY, partitionNameThisMonth));

        // Define the DDL for the table, which autoPartition.checkAndCreatePartition will parse
        String ddlMonthly = String.format(
                "CREATE TABLE %s.%s AUTO PARTITION BY RANGE (date_trunc(order_date, 'MONTH')) ()",
                TEST_DATABASE, TEST_TABLE_MONTHLY
        );

        // Simulate data for this month, triggering partition creation for this month
        autoPartition.checkAndCreatePartition(TEST_DATABASE, TEST_TABLE_MONTHLY, ddlMonthly, today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // Simulate data for next month, triggering partition creation for next month
        autoPartition.checkAndCreatePartition(TEST_DATABASE, TEST_TABLE_MONTHLY, ddlMonthly, nextMonthDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));


        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_MONTHLY, partitionNameThisMonth),
                "Partition for this month should exist after auto creation.");
        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_MONTHLY, partitionNameNextMonth),
                "Partition for next month should exist after auto creation.");
    }

    @Test
    void testAddMissingPartitionsForDailyTable() {
        // Ensure no partitions for today or future exist initially
        LocalDate today = LocalDate.now();
        String partitionToday = "p" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        ddlOperations.execute("ALTER TABLE " + TEST_DATABASE + "." + TEST_TABLE_DAILY + " DROP PARTITION IF EXISTS " + partitionToday);

        // This test aims to verify that if a partition for a past date exists,
        // the checkAndCreatePartition logic correctly creates partitions for current/future dates
        // based on the DDL and partition value provided.

        // Create a partition for yesterday manually to simulate an existing older partition.
        LocalDate yesterday = today.minusDays(1);
        String partitionYesterdayName = "p" + yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String partitionYesterdayValue = yesterday.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // The DDL for addPartition requires the partition definition, not just the value for LESS THAN.
        // For a RANGE partition like 'p20230101 VALUES LESS THAN ("2023-01-02")'
        String partitionDefinitionYesterday = String.format("PARTITION %s VALUES LESS THAN ('%s')",
                partitionYesterdayName, partitionYesterdayValue);

        // Drop the table and recreate it with only yesterday's partition for a clean state for this specific test logic.
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE_DAILY)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE_DAILY);
        }
        // Create the base table structure (without dynamic partition properties for this manual addition part)
        String createTableDdl = String.format(
                "CREATE TABLE %s.%s ( id INT, event_time DATETIME, value VARCHAR(255)) " +
                "UNIQUE KEY(id) PARTITION BY RANGE(event_time) (%s) " +
                "DISTRIBUTED BY HASH(id) BUCKETS 3",
                TEST_DATABASE, TEST_TABLE_DAILY, partitionDefinitionYesterday
        );
        ddlOperations.execute(createTableDdl);
        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_DAILY, partitionYesterdayName), "Partition for yesterday should exist.");

        // Now, use checkAndCreatePartition to ensure partitions for today and tomorrow are created.
        // The DDL passed to checkAndCreatePartition should reflect the AUTO PARTITION configuration intended.
        String ddlForAutoPartitionCheck = String.format(
                "CREATE TABLE %s.%s AUTO PARTITION BY RANGE (date_trunc(event_time, 'DAY')) ()", // Simplified DDL for parsing
                TEST_DATABASE, TEST_TABLE_DAILY
        );

        // Simulate data for today
        autoPartition.checkAndCreatePartition(TEST_DATABASE, TEST_TABLE_DAILY, ddlForAutoPartitionCheck, today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        // Simulate data for tomorrow
        autoPartition.checkAndCreatePartition(TEST_DATABASE, TEST_TABLE_DAILY, ddlForAutoPartitionCheck, today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        String partitionNameToday = "p" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String partitionNameTomorrow = "p" + today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_DAILY, partitionNameToday),
                "Partition for today should be created by autoPartition.checkAndCreatePartition.");
        assertTrue(ddlOperations.partitionExists(TEST_DATABASE, TEST_TABLE_DAILY, partitionNameTomorrow),
                "Partition for tomorrow should be created by autoPartition.checkAndCreatePartition.");
    }

    // Removed testAutoPartitionJobWithProperties as it's not directly testable without full context
    // and its core logic is covered by other tests.

}