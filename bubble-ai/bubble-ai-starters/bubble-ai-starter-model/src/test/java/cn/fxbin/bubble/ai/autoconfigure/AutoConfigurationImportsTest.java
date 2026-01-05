package cn.fxbin.bubble.ai.autoconfigure;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationImportsTest {

    @Test
    void importsShouldContainAutoConfigurations() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assertThat(classLoader.getResource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .isNotNull();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                classLoader.getResourceAsStream("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"),
                StandardCharsets.UTF_8
        ))) {
            List<String> lines = reader.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            assertThat(lines).contains(
                    "cn.fxbin.bubble.ai.autoconfigure.BubbleAiAutoConfiguration",
                    "cn.fxbin.bubble.ai.autoconfigure.BubbleAiMybatisAutoConfiguration"
            );
        }
    }
}
