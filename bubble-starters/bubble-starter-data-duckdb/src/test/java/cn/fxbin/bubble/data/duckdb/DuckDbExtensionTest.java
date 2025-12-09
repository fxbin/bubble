package cn.fxbin.bubble.data.duckdb;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbAutoConfiguration;
import cn.fxbin.bubble.data.duckdb.core.DuckDbTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DuckDB Extension Test
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 16:00
 */
@SpringBootTest(classes = DuckDbAutoConfiguration.class)
@TestPropertySource(properties = {
    "dm.data.duckdb.mode=MEMORY",
    "dm.data.duckdb.extensions[0]=json",
    "dm.data.duckdb.auto-install-extensions=true"
})
public class DuckDbExtensionTest {

    @Autowired
    private DuckDbTemplate duckDbTemplate;

    @Test
    void testJsonExtension() {
        // This query requires the json extension
        // Note: json_extract returns a JSON type, which JDBC driver maps to String
        String sql = "SELECT json_extract('{\"foo\": \"bar\"}', '$.foo') as result";
        
        List<Map<String, Object>> result = duckDbTemplate.queryForList(sql);
        
        assertThat(result).hasSize(1);
        // The result of json_extract('{"foo": "bar"}', '$.foo') is '"bar"' (with quotes) because it's a JSON string.
        // If we used json_extract_string, it would be 'bar'.
        // Let's check what we get.
        Object val = result.get(0).get("result");
        assertThat(val).isNotNull();
        System.out.println("JSON Extract Result: " + val);
    }
}
