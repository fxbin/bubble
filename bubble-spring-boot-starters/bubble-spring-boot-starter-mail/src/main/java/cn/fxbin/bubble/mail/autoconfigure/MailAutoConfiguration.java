package cn.fxbin.bubble.mail.autoconfigure;

import cn.fxbin.bubble.core.util.CollectionUtils;
import cn.fxbin.bubble.mail.MailProperties;
import jakarta.annotation.Resource;
import org.dromara.email.api.MailClient;
import org.dromara.email.comm.config.MailSmtpConfig;
import org.dromara.email.core.factory.MailFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * MailAutoConfiguration
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/7 19:38
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnProperty(prefix = MailProperties.BUBBLE_MAIL_PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MailProperties.class)
public class MailAutoConfiguration implements InitializingBean {

    @Resource
    private MailProperties properties;

    @Bean
    @Lazy
    public MailClient mailClient() {
        MailSmtpConfig mailSmtpConfig = MailSmtpConfig.builder()
                .port(properties.getPort())
                .smtpServer(properties.getSmtpServer())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .fromAddress(properties.getFromAddress())
                .isSSL(properties.getIsSsl())
                .isAuth(properties.getIsAuth())
                .build();
        MailFactory.put("default", mailSmtpConfig);
        return MailFactory.createMailClient("default");
    }

    /**
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 多租户配置
        if (properties.isEnabled() && CollectionUtils.isNotEmpty(properties.getTenant())) {


            Assert.isNull(properties.getPrimary(), "bubble.mail.primary 属性不允许为空");
            Assert.isTrue(properties.getTenant().containsKey(properties.getPrimary()),
                    "bubble.mail.tenant 属性配置未包含 bubble.mail.primary 指定value");

            Map<String, MailSmtpConfig> tenantConfig = properties.getTenant();
            tenantConfig.forEach(MailFactory::put);
        }
    }
}