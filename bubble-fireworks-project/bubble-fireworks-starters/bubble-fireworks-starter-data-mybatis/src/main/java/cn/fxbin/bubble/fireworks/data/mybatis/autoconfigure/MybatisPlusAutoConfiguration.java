package cn.fxbin.bubble.fireworks.data.mybatis.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
        basePackages = {"cn.fxbin.bubble.fireworks.data.mybatis"}
)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class, MapperFactoryBean.class})
@EnableTransactionManagement
public class MybatisPlusAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }

    @Bean
    public MybatisPlusPropertiesCustomizer idTypeCustomizer() {
        return plusProperties -> plusProperties.getGlobalConfig().getDbConfig().setIdType(IdType.AUTO);
    }

}
