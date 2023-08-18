package cn.fxbin.bubble.data.mybatisplus.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * DataSourceConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/10/29 5:05 下午
 */
@Configuration(
        proxyBeanMethods = false
)
public class DataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("spring.datasource.hikari.jdbc-url")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }


}
