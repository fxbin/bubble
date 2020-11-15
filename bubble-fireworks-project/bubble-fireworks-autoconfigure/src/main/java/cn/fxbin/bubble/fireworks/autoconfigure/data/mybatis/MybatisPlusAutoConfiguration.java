package cn.fxbin.bubble.fireworks.autoconfigure.data.mybatis;

import cn.fxbin.bubble.fireworks.data.mybatis.customizer.CustomizeIdentifierGenerator;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MybatisPlusConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 18:53
 */
@Configuration(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackages = {"cn.fxbin.bubble.fireworks.autoconfigure.data.mybatis"}
)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class, MapperFactoryBean.class})
@EnableTransactionManagement
public class MybatisPlusAutoConfiguration {

    /**
     * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false
     * 避免缓存出现问题(该属性会在旧插件移除后一同移除)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({CustomizeIdentifierGenerator.class})
    public MybatisPlusPropertiesCustomizer identifierGeneratorCustomizer() {
        return plusProperties -> plusProperties.getGlobalConfig().setIdentifierGenerator(new CustomizeIdentifierGenerator());
    }

    @Bean
    public MybatisPlusPropertiesCustomizer idTypeCustomizer() {
        return plusProperties -> plusProperties.getGlobalConfig().getDbConfig().setIdType(IdType.AUTO);
    }
}
