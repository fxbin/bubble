package cn.fxbin.bubble.data.duckdb.core;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuckDbIngesterTest {

    private HikariDataSource dataSource;

    @AfterEach
    void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void shouldIngestRowsWithAppender() {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.MEMORY);
        properties.setMaximumPoolSize(1);

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        dataSource = factory.createDefaultDataSource();

        DuckDbTemplate template = new DuckDbTemplate(dataSource);
        template.execute("CREATE TABLE events (id BIGINT, name VARCHAR, created_at TIMESTAMP)");

        DuckDbIngester ingester = new DuckDbIngester(dataSource);
        ingester.append("events", List.<Object[]>of(
                new Object[]{1L, "A", LocalDateTime.of(2025, 12, 17, 12, 0, 0)},
                new Object[]{2L, "B", "2025-12-17 13:00:00"}
        ));

        Long count = template.queryForObject("SELECT count(*) FROM events", Long.class);
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void shouldFailOnColumnCountMismatch() {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.MEMORY);
        properties.setMaximumPoolSize(1);

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        dataSource = factory.createDefaultDataSource();

        DuckDbTemplate template = new DuckDbTemplate(dataSource);
        template.execute("CREATE TABLE t (id BIGINT, name VARCHAR)");

        DuckDbIngester ingester = new DuckDbIngester(dataSource);
        assertThatThrownBy(() -> ingester.append("t", List.<Object[]>of(new Object[]{1L})))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldExportParquetWhenPathContainsSingleQuote(@TempDir Path tempDir) {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.MEMORY);
        properties.setMaximumPoolSize(1);

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        dataSource = factory.createDefaultDataSource();

        DuckDbTemplate template = new DuckDbTemplate(dataSource);
        template.execute("CREATE TABLE t (id BIGINT)");
        template.execute("INSERT INTO t VALUES (1)");

        Path output = tempDir.resolve("a'b.parquet");
        template.exportParquet("t", output.toString());

        assertThat(output).exists();
    }
}
