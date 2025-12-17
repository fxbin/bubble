package cn.fxbin.bubble.data.duckdb.autoconfigure;

import cn.fxbin.bubble.data.duckdb.DuckDbOperations;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuckDbAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DuckDbAutoConfiguration.class));

    @Test
    void shouldNotLoadWhenDisabled() {
        contextRunner
                .withPropertyValues("dm.data.duckdb.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DuckDbOperations.class);
                    assertThat(context).doesNotHaveBean("duckDbDataSource");
                });
    }

    @Test
    void shouldCreateBeansAndOperateOnDatabase(@TempDir Path tempDir) {
        Path dbPath = tempDir.resolve("test.duckdb");
        contextRunner
                .withPropertyValues(
                        "dm.data.duckdb.enabled=true",
                        "dm.data.duckdb.mode=FILE",
                        "dm.data.duckdb.file-path=" + dbPath,
                        "dm.data.duckdb.maximum-pool-size=2"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(DuckDbOperations.class);
                    assertThat(context).hasBean("duckDbDataSource");

                    Object ds = context.getBean("duckDbDataSource");
                    assertThat(ds).isInstanceOf(HikariDataSource.class);
                    HikariDataSource hikari = (HikariDataSource) ds;
                    assertThat(hikari.getJdbcUrl()).contains("jdbc:duckdb:");

                    DuckDbOperations ops = context.getBean(DuckDbOperations.class);
                    ops.execute("CREATE TABLE users (id BIGINT, name VARCHAR)");
                    ops.execute("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob')");
                    Long count = ops.count("users");
                    assertThat(count).isEqualTo(2L);

                    assertThatThrownBy(() -> ops.count("users; DROP TABLE users"))
                            .isInstanceOf(IllegalArgumentException.class);
                });
    }
}
