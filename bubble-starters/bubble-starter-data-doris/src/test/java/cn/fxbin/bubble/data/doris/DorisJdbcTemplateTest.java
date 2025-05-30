package cn.fxbin.bubble.data.doris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DorisJdbcTemplateTest
 *
 * <p>
 * Integration tests for JdbcTemplate and NamedParameterJdbcTemplate with Doris.
 * Tests basic JDBC operations and Doris-specific SQL features.
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/29 11:30
 */
public class DorisJdbcTemplateTest extends BaseDorisIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JdbcDorisDdlOperations ddlOperations;

    private static final String TEST_DATABASE = "test_db_jdbc";
    private static final String TEST_TABLE = "test_jdbc_operations";

    @BeforeEach
    void setUp() {
        ddlOperations.execute("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
        ddlOperations.execute("USE " + TEST_DATABASE);

        // Clean up and recreate test table
        if (ddlOperations.tableExists(TEST_DATABASE, TEST_TABLE)) {
            ddlOperations.dropTable(TEST_DATABASE, TEST_TABLE);
        }

        // Create a simple test table
        String createTableSql = String.format("""
            CREATE TABLE IF NOT EXISTS %s.%s (
                id INT NOT NULL COMMENT 'Primary key',
                name VARCHAR(50) NOT NULL COMMENT 'User name',
                age INT NULL COMMENT 'User age',
                score DECIMAL(10,2) NULL COMMENT 'User score',
                created_at DATETIME NOT NULL COMMENT 'Creation time'
            ) ENGINE=OLAP
            UNIQUE KEY(id)
            DISTRIBUTED BY HASH(id) BUCKETS 1
            PROPERTIES (
                "replication_allocation" = "tag.location.default: 1",
                "in_memory" = "false",
                "storage_format" = "V2"
            )
            """, TEST_DATABASE, TEST_TABLE);

        ddlOperations.execute(createTableSql);
    }

    @Test
    void testBasicJdbcOperations() {
        // Test insert
        String insertSql = String.format(
                "INSERT INTO %s.%s (id, name, age, score, created_at) VALUES (?, ?, ?, ?, NOW())",
                TEST_DATABASE, TEST_TABLE
        );
        jdbcTemplate.update(insertSql, 1, "Alice", 25, 95.5);

        // Test select
        String selectSql = String.format("SELECT * FROM %s.%s WHERE id = ?", TEST_DATABASE, TEST_TABLE);
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, 1);

        assertEquals(1, result.get("id"));
        assertEquals("Alice", result.get("name"));
        assertEquals(25, result.get("age"));
        assertEquals(95.5, ((Number) result.get("score")).doubleValue(), 0.01);
        assertNotNull(result.get("created_at"));

        // Test update
        String updateSql = String.format(
                "UPDATE %s.%s SET age = ?, score = ? WHERE id = ?",
                TEST_DATABASE, TEST_TABLE
        );
        jdbcTemplate.update(updateSql, 26, 97.5, 1);

        // Verify update
        result = jdbcTemplate.queryForMap(selectSql, 1);
        assertEquals(26, result.get("age"));
        assertEquals(97.5, ((Number) result.get("score")).doubleValue(), 0.01);

        // Test delete
        String deleteSql = String.format("DELETE FROM %s.%s WHERE id = ?", TEST_DATABASE, TEST_TABLE);
        jdbcTemplate.update(deleteSql, 1);

        // Verify delete
        int count = jdbcTemplate.queryForObject(
                String.format("SELECT COUNT(*) FROM %s.%s WHERE id = ?", TEST_DATABASE, TEST_TABLE),
                Integer.class,
                1
        );
        assertEquals(0, count);
    }

    @Test
    void testNamedParameterJdbcTemplate() {
        // Test batch insert with named parameters
        String insertSql = String.format("""
            INSERT INTO %s.%s (id, name, age, score, created_at)
            VALUES (:id, :name, :age, :score, NOW())
            """, TEST_DATABASE, TEST_TABLE);

        MapSqlParameterSource params1 = new MapSqlParameterSource()
                .addValue("id", 1)
                .addValue("name", "Bob")
                .addValue("age", 30)
                .addValue("score", 88.5);

        MapSqlParameterSource params2 = new MapSqlParameterSource()
                .addValue("id", 2)
                .addValue("name", "Carol")
                .addValue("age", 28)
                .addValue("score", 92.0);

        namedParameterJdbcTemplate.update(insertSql, params1);
        namedParameterJdbcTemplate.update(insertSql, params2);

        // Test select with named parameters
        String selectSql = String.format("""
            SELECT * FROM %s.%s
            WHERE age >= :minAge AND score >= :minScore
            ORDER BY score DESC
            """, TEST_DATABASE, TEST_TABLE);

        MapSqlParameterSource queryParams = new MapSqlParameterSource()
                .addValue("minAge", 25)
                .addValue("minScore", 85.0);

        List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(selectSql, queryParams);

        assertEquals(2, results.size());
        assertEquals("Carol", results.get(0).get("name")); // Higher score should be first
        assertEquals("Bob", results.get(1).get("name"));
    }

    @Test
    void testDorisSqlFeatures() {
        // Insert test data
        String insertSql = String.format("""
            INSERT INTO %s.%s (id, name, age, score, created_at)
            VALUES
                (1, 'Alice', 25, 95.5, '2024-01-01 10:00:00'),
                (2, 'Bob', 30, 88.5, '2024-01-02 11:00:00'),
                (3, 'Carol', 28, 92.0, '2024-01-03 12:00:00')
            """, TEST_DATABASE, TEST_TABLE);

        jdbcTemplate.update(insertSql);

        // Test Doris window functions
        String windowSql = String.format("""
            SELECT
                name,
                score,
                RANK() OVER (ORDER BY score DESC) as score_rank,
                AVG(score) OVER (ORDER BY score DESC ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) as moving_avg
            FROM %s.%s
            ORDER BY score DESC
            """, TEST_DATABASE, TEST_TABLE);

        List<Map<String, Object>> windowResults = jdbcTemplate.queryForList(windowSql);
        assertEquals(3, windowResults.size());
        assertEquals(1, windowResults.get(0).get("score_rank")); // Alice should be rank 1

        // Test Doris aggregate functions
        String aggSql = String.format("""
            SELECT
                COUNT(*) as total_count,
                AVG(age) as avg_age,
                MIN(score) as min_score,
                MAX(score) as max_score,
                SUM(score) as total_score
            FROM %s.%s
            """, TEST_DATABASE, TEST_TABLE);

        Map<String, Object> aggResults = jdbcTemplate.queryForMap(aggSql);
        assertEquals(3L, aggResults.get("total_count"));
        assertTrue(((Number) aggResults.get("avg_age")).doubleValue() > 27.0);
        assertEquals(88.5, ((Number) aggResults.get("min_score")).doubleValue(), 0.01);
        assertEquals(95.5, ((Number) aggResults.get("max_score")).doubleValue(), 0.01);

        // Test Doris date functions
        String dateSql = String.format("""
            SELECT
                name,
                created_at,
                DATE_FORMAT(created_at, '%%Y-%%m-%%d') as date_only,
                DAYOFWEEK(created_at) as day_of_week
            FROM %s.%s
            WHERE created_at >= '2024-01-01'
            ORDER BY created_at
            """, TEST_DATABASE, TEST_TABLE);

        List<Map<String, Object>> dateResults = jdbcTemplate.queryForList(dateSql);
        assertEquals(3, dateResults.size());
        assertTrue(dateResults.get(0).get("date_only").toString().startsWith("2024-01-"));
    }
}