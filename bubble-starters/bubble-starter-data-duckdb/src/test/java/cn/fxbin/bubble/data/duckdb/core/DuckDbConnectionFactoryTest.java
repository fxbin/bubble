package cn.fxbin.bubble.data.duckdb.core;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuckDbConnectionFactoryTest {

    @Test
    void shouldBuildJdbcUrlFromFileMode(@TempDir Path tempDir) {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.FILE);
        properties.setFilePath(tempDir.resolve("db.duckdb").toString());
        properties.setReadOnly(true);
        properties.setMaximumPoolSize(3);
        properties.setMemoryLimit("2GB");
        properties.setThreads("2");
        properties.setTempDirectory(tempDir.resolve("tmp").toString());

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        HikariDataSource ds = factory.createDefaultDataSource();

        assertThat(ds.getJdbcUrl()).contains("jdbc:duckdb:").contains("duckdb.read_only=true");
        assertThat(ds.getMaximumPoolSize()).isEqualTo(3);
        assertThat(ds.getDataSourceProperties())
                .containsEntry("memory_limit", "2GB")
                .containsEntry("threads", "2")
                .containsEntry("temp_directory", properties.getTempDirectory())
                .containsEntry("preserve_insertion_order", "false");

        ds.close();
    }

    @Test
    void shouldUseCustomJdbcUrlWhenProvided() {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setUrl("jdbc:duckdb:");

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        HikariDataSource ds = factory.createDefaultDataSource();

        assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:duckdb:");
        ds.close();
    }

    @Test
    void shouldSetConnectionInitSqlForExtensions() {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.MEMORY);
        properties.setExtensions(List.of("json"));

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        HikariDataSource ds = factory.createDefaultDataSource();

        assertThat(ds.getConnectionInitSql()).contains("LOAD json");
        ds.close();
    }

    @Test
    void shouldFailWhenFileModeWithoutFilePath() {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.FILE);
        properties.setFilePath(" ");

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        assertThatThrownBy(factory::createDefaultDataSource)
                .isInstanceOf(IllegalArgumentException.class);
    }
}
