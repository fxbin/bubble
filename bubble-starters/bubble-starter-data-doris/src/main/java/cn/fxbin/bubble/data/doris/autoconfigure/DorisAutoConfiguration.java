package cn.fxbin.bubble.data.doris.autoconfigure;

import cn.fxbin.bubble.data.doris.*;
import cn.fxbin.bubble.data.doris.client.DorisHttpClient;
import com.dtflys.forest.Forest;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * DorisAutoConfiguration
 * Doris 自动配置类，配置 Doris 数据源、JdbcTemplate、DDL 操作和自动分区管理。
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Forest.class, JdbcTemplate.class, HikariDataSource.class, NamedParameterJdbcTemplate.class})
@EnableConfigurationProperties({DorisProperties.class})
@ConditionalOnProperty(prefix = "bubble.data.doris", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DorisAutoConfiguration {

    private final DorisProperties dorisProperties;

    public DorisAutoConfiguration(DorisProperties dorisProperties) {
        this.dorisProperties = dorisProperties;
    }

    /**
     * 配置 Doris 数据源
     *
     * @return Doris 数据源
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSource dorisDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dorisProperties.getJdbcUrl());
        dataSource.setUsername(dorisProperties.getUsername());
        dataSource.setPassword(dorisProperties.getPassword());
        dataSource.setMaximumPoolSize(dorisProperties.getPool().getMaxPoolSize());
        dataSource.setMinimumIdle(dorisProperties.getPool().getMinIdle());
        dataSource.setMaxLifetime(dorisProperties.getPool().getMaxLifetime());
        dataSource.setConnectionTimeout(dorisProperties.getPool().getConnectionTimeout());
        dataSource.setIdleTimeout(dorisProperties.getPool().getIdleTimeout());
        dataSource.setConnectionTestQuery("SELECT 1"); // Doris 连接测试查询
        return dataSource;
    }

    /**
     * 配置 Doris JdbcTemplate
     *
     * @param dorisDataSource Doris 数据源
     * @return JdbcTemplate 实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "dorisJdbcTemplate")
    public JdbcTemplate dorisJdbcTemplate(DataSource dorisDataSource) {
        return new JdbcTemplate(dorisDataSource);
    }

    /**
     * 配置 Doris NamedParameterJdbcTemplate
     *
     * @param dorisDataSource Doris 数据源
     * @return NamedParameterJdbcTemplate 实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "dorisNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate dorisNamedParameterJdbcTemplate(DataSource dorisDataSource) {
        return new NamedParameterJdbcTemplate(dorisDataSource);
    }

    /**
     * 配置 Doris DDL 操作接口及 DorisTableDdlOperations 接口实现
     *
     * @param dorisNamedParameterJdbcTemplate Doris NamedParameterJdbcTemplate
     * @return JdbcDorisDdlOperations 实例，同时实现了 DorisDdlOperations 和 DorisTableDdlOperations
     */
    @Bean
    @ConditionalOnMissingBean
    public JdbcDorisDdlOperations dorisDdlOperations(NamedParameterJdbcTemplate dorisNamedParameterJdbcTemplate) {
        return new JdbcDorisDdlOperations(dorisNamedParameterJdbcTemplate);
    }

    /**
     * 配置 Doris 表自动分区管理器
     *
     * @param dorisDdlOperations Doris DDL 操作接口
     * @return DorisTableAutoPartition 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "bubble.data.doris.auto-partition", name = "enabled", havingValue = "true")
    public DorisTableAutoPartition dorisTableAutoPartition(DorisDdlOperations dorisDdlOperations) {
        return new DorisTableAutoPartition(dorisDdlOperations, dorisProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DorisHttpClient dorisHttpClient(DorisProperties properties) {
        // Forest will automatically create a proxy implementation of the interface
        // Ensure ForestProperties are configured if specific Forest settings are needed
        // For example, setting the backend, timeouts, etc., can be done via ForestProperties
        // or programmatically on the ForestConfiguration object.
        log.info("Configuring DorisHttpClient with FE Host: {}, HTTP Port: {}", properties.getFeHost(), properties.getFeHttpPort());
        return Forest.client(DorisHttpClient.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public DorisStreamLoadOperations dorisStreamLoadOperations(
            DorisHttpClient dorisHttpClient,
            DorisProperties dorisProperties,
            JdbcTemplate dorisJdbcTemplate,
            DorisTableDdlOperations dorisTableDdlOperations) {
        log.info("Configuring HttpDorisStreamLoadOperations");
        return new HttpDorisStreamLoadOperations(
                dorisHttpClient,
                dorisProperties,
                dorisJdbcTemplate,
                dorisTableDdlOperations);
    }

}