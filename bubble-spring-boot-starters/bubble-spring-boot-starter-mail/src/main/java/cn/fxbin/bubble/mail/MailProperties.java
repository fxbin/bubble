package cn.fxbin.bubble.mail;

import com.google.common.collect.Maps;
import lombok.Data;
import org.dromara.email.comm.config.MailSmtpConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

import static cn.fxbin.bubble.mail.MailProperties.BUBBLE_MAIL_PREFIX;

/**
 * MailProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/9/7 19:33
 */
@Data
@ConfigurationProperties(prefix = BUBBLE_MAIL_PREFIX)
public class MailProperties {

    public static final String BUBBLE_MAIL_PREFIX = "bubble.mail";


    /**
     * 是否开启 mail，默认：true
     */
    private boolean enabled = true;

    /**
     * 端口号
     * */
    private String port;

    /**
     * 发件人地址
     * */
    private String fromAddress;

    /**
     * 服务器地址
     * */
    private String smtpServer;

    /**
     * 账号
     * */
    private String username;

    /**
     * 密码
     * */
    private String password;

    /**
     * 是否开启ssl 默认开启
     * */
    private String isSsl = "true";

    /**
     * 是否开启验证 默认开启
     * */
    private String isAuth = "true";

    /**
     * 多租户配置时需要配置此项，指定默认主租户
     */
    private String primary;

    /**
     * 多租户配置
     */
    private Map<String, MailSmtpConfig> tenant = Maps.newHashMap();

}