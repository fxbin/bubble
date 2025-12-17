package cn.fxbin.bubble.data.duckdb.autoconfigure;

import cn.fxbin.bubble.data.duckdb.DuckDbOperations;
import cn.fxbin.bubble.data.duckdb.core.DuckDbConnectionFactory;
import cn.fxbin.bubble.data.duckdb.core.DuckDbIngester;
import cn.fxbin.bubble.data.duckdb.core.DuckDbManager;
import cn.fxbin.bubble.data.duckdb.core.DuckDbTemplate;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.duckdb.DuckDBDriver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * DuckDB 自动配置
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 11:42
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({DuckDBDriver.class, DataSource.class})
@EnableConfigurationProperties(DuckDbProperties.class)
@ConditionalOnProperty(prefix = "dm.data.duckdb", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class DuckDbAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DuckDbConnectionFactory duckDbConnectionFactory(DuckDbProperties properties) {
        return new DuckDbConnectionFactory(properties);
    }

    /**
     * 定义 DuckDB 专用数据源
     * <p>
     * 注意：这里不使用 @Primary，也不使用 @ConditionalOnMissingBean(DataSource.class)，
     * 以确保它能作为“第二数据源”与主业务库（如 MySQL）共存。
     * </p>
     */
    @Bean("duckDbDataSource")
    public DataSource duckDbDataSource(DuckDbConnectionFactory connectionFactory, DuckDbProperties properties) {
        if (properties.getMode() == DuckDbProperties.Mode.FILE && !properties.isReadOnly()) {
            log.warn("DuckDB 配置为 FILE 模式，具有 READ_WRITE 访问权限。" +
                    "确保没有其他进程正在访问 '{}'。DuckDB 强制执行单一写入者策略。", properties.getFilePath());
        }

        HikariDataSource dataSource = connectionFactory.createDefaultDataSource();
        log.info("DuckDB 数据源已初始化: {}", dataSource.getJdbcUrl());
        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public DuckDbTemplate duckDbTemplate(@Qualifier("duckDbDataSource") DataSource dataSource) {
        return new DuckDbTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public DuckDbManager duckDbManager(DuckDbProperties properties) {
        return new DuckDbManager(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DuckDbIngester duckDbIngester(@Qualifier("duckDbDataSource") DataSource dataSource) {
        return new DuckDbIngester(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public DuckDbOperations duckDbOperations(DuckDbTemplate duckDbTemplate, DuckDbManager duckDbManager, DuckDbIngester duckDbIngester) {
        return new DuckDbOperations(duckDbTemplate, duckDbManager, duckDbIngester);
    }

}