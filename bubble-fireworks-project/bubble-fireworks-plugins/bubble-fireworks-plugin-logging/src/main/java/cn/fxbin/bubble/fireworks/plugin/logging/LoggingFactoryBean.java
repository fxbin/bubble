package cn.fxbin.bubble.fireworks.plugin.logging;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.net.InetAddress;

/**
 * LoggingFactoryBean
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 16:16
 */
@Component
public class LoggingFactoryBean implements EnvironmentAware, InitializingBean {

    /**
     * applicationContext config environment
     * Affected by "spring.profiles.active" config properties
     * {@link EnvironmentAware}
     */
    @Nullable
    private Environment environment;

    /**
     * The name of the SpringBoot Application Name
     */
    private static final String APPLICATION_NAME = "spring.application.name";

    /**
     * The service of the SpringBoot Server port
     */
    private static final String SERVER_PORT = "server.port";

    /**
     * 服务名称
     */
    private String serviceId;

    /**
     * 服务端口
     */
    private String servicePort;

    /**
     * 服务IP
     */
    private String serviceIp;

    public String getServiceId() {
        return serviceId;
    }

    public String getServicePort() {
        return servicePort;
    }

    public String getServiceIp() {
        return serviceIp;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.serviceId = environment.getProperty(APPLICATION_NAME);
        Assert.notNull(this.serviceId, "Please add the 【spring.application.name】 configuration in the application.yml or application.properties");
        this.servicePort = environment.getProperty(SERVER_PORT);
        Assert.notNull(this.servicePort, "Please add the 【server.port】 configuration in the application.yml or application.properties");
        InetAddress inetAddress = InetAddress.getLocalHost();
        this.serviceIp = inetAddress.getHostAddress();
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
