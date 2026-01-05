package cn.fxbin.bubble.ai.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BubbleAiMybatisAutoConfigurationTest {

    @Test
    void shouldBackOffWhenMybatisClassesMissing() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader(
                        "com.baomidou.mybatisplus.core.mapper.BaseMapper",
                        "org.mybatis.spring.annotation.MapperScan"
                ))
                .withConfiguration(AutoConfigurations.of(BubbleAiMybatisAutoConfiguration.class))
                .run(context -> assertThat(context.getStartupFailure()).isNull());
    }
}

