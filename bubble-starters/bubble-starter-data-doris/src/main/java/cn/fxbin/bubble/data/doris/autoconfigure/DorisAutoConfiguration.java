package cn.fxbin.bubble.data.doris.autoconfigure;

import cn.fxbin.bubble.data.doris.DorisDdlOperations;
import cn.fxbin.bubble.data.doris.DorisTableAutoPartition;
import cn.fxbin.bubble.data.doris.DorisTableDdlOperations;
import cn.fxbin.bubble.data.doris.JdbcDorisDdlOperations;
import com.zaxxer.hikari.HikariDataSource;
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({JdbcTemplate.class, HikariDataSource.class, NamedParameterJdbcTemplate.class})
@EnableConfigurationProperties(DorisProperties.class)
@ConditionalOnProperty(prefix = DorisProperties.PREFIX, name = "enabled", havingValue = "true")
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
        dataSource.setJdbcUrl(dorisProperties.getUrl());
        dataSource.setUsername(dorisProperties.getUsername());
        dataSource.setPassword(dorisProperties.getPassword());
        dataSource.setMaximumPoolSize(dorisProperties.getMaxActive());
        dataSource.setConnectionTimeout(dorisProperties.getConnectionTimeout().toMillis());
        dataSource.setMaxLifetime(dorisProperties.getMaxWait().toMillis());
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
    @ConditionalOnProperty(prefix = DorisProperties.PREFIX + ".auto-partition", name = "enabled", havingValue = "true")
    public DorisTableAutoPartition dorisTableAutoPartition(DorisDdlOperations dorisDdlOperations) {
        return new DorisTableAutoPartition(dorisDdlOperations, dorisProperties);
    }

}