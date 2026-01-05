package cn.fxbin.bubble.ai.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Bubble AI 的 MyBatis Mapper 自动扫描配置。
 *
 * <p>作用：在引入 bubble-ai-starter-model 的业务工程中，无需额外配置即可自动扫描
 * {@link #MAPPER_BASE_PACKAGE} 下的 Mapper（例如 {@code AiModelConfigMapper}）。</p>
 *
 * <p>不冲突策略：
 * <ul>
 *     <li>只扫描固定的独立包路径，不会覆盖业务侧的 MapperScan。</li>
 *     <li>仅当容器中已存在 {@code SqlSessionFactory} 时才生效，避免非 MyBatis 场景报错。</li>
 * </ul>
 * </p>
 *
 * @author fxbin
 */
@AutoConfiguration(afterName = {
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"
})
@ConditionalOnClass(name = {
        BubbleAiMybatisAutoConfiguration.MYBATIS_BASE_MAPPER_X,
        BubbleAiMybatisAutoConfiguration.MYBATIS_MAPPER_SCANNER_CONFIGURER,
})
@ConditionalOnBean(type = BubbleAiMybatisAutoConfiguration.MYBATIS_SQL_SESSION_FACTORY)
@ConditionalOnProperty(prefix = BubbleAiMybatisAutoConfiguration.MODEL_CONFIG_PROPERTY_PREFIX,
        name = BubbleAiMybatisAutoConfiguration.ENABLED_PROPERTY_NAME,
        havingValue = "true",
        matchIfMissing = false)
public class BubbleAiMybatisAutoConfiguration {

    public static final String MODEL_CONFIG_PROPERTY_PREFIX = "bubble.ai.model-config";

    public static final String ENABLED_PROPERTY_NAME = "enabled";

    public static final String MAPPER_BASE_PACKAGE = "cn.fxbin.bubble.ai.mapper";

    public static final String MYBATIS_BASE_MAPPER_X = "cn.fxbin.bubble.data.mybatisplus.mapper.BaseMapperX";

    public static final String MYBATIS_MAPPER_SCANNER_CONFIGURER = "org.mybatis.spring.mapper.MapperScannerConfigurer";

    public static final String MYBATIS_SQL_SESSION_FACTORY = "org.apache.ibatis.session.SqlSessionFactory";

    /**
     * 创建 Mapper 扫描器配置。
     *
     * <p>作用：注册一个 {@code MapperScannerConfigurer}，扫描 {@link #MAPPER_BASE_PACKAGE} 下的 Mapper。</p>
     *
     * <p>参数：无。</p>
     *
     * <p>返回：{@code MapperScannerConfigurer} 实例（以 {@code Object} 形式返回，避免对 MyBatis 产生编译期强依赖）。</p>
     *
     * <p>异常：反射创建失败时抛出 {@link IllegalStateException}。</p>
     *
     * @author fxbin
     */
    @Bean
    public static Object bubbleAiMapperScannerConfigurer() {
        try {
            Class<?> mapperScannerConfigurerClass = Class.forName(MYBATIS_MAPPER_SCANNER_CONFIGURER);
            Object configurer = mapperScannerConfigurerClass.getDeclaredConstructor().newInstance();
            mapperScannerConfigurerClass.getMethod("setBasePackage", String.class)
                    .invoke(configurer, MAPPER_BASE_PACKAGE);
            return configurer;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * DDL 自动维护
     *
     * @return {@link BubbleAiDdl}
     */
    @Bean
    @ConditionalOnClass(name = "com.baomidou.mybatisplus.extension.ddl.IDdl")
    @ConditionalOnMissingBean
    public BubbleAiDdl bubbleAiDdl() {
        return new BubbleAiDdl();
    }
}
