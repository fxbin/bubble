package cn.fxbin.bubble.data.duckdb.core;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DuckDbManagerTest {

    private DuckDbManager manager;

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.close();
        }
    }

    @Test
    void shouldCacheTemplatesByPathAndMode(@TempDir Path tempDir) {
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.FILE);
        properties.setFilePath(tempDir.resolve("default.duckdb").toString());

        manager = new DuckDbManager(properties);

        String path = tempDir.resolve("a.duckdb").toString();
        DuckDbTemplate rw1 = manager.getTemplate(path, false);
        DuckDbTemplate rw2 = manager.getTemplate(path, false);
        DuckDbTemplate ro = manager.getTemplate(path, true);

        assertThat(rw1).isSameAs(rw2);
        assertThat(ro).isNotSameAs(rw1);

        rw1.execute("CREATE TABLE t (id BIGINT)");
        Long count = rw1.queryForObject("SELECT count(*) FROM t", Long.class);
        assertThat(count).isEqualTo(0L);
    }
}

