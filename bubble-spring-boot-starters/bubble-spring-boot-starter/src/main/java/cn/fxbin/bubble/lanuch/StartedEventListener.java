package cn.fxbin.bubble.lanuch;

import cn.fxbin.bubble.core.util.support.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static cn.fxbin.bubble.core.util.support.AppUtils.*;

/**
 * StartedEventListener
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/12/2 10:05 上午
 */
@Slf4j
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ComponentScan(basePackages = {"cn.fxbin.bubble"})
@Configuration(
        proxyBeanMethods = false
)
public class StartedEventListener {

    /**
     * Application Name
     */
    private final static String APPLICATION_NAME = "spring.application.name";

    /**
     * context-path
     */
    private final static String CONTEXT_PATH = "server.servlet.context-path";

    @Async
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(WebServerInitializedEvent event) {
        WebServerApplicationContext context = event.getApplicationContext();
        Environment environment = context.getEnvironment();
        String appName = Optional.ofNullable(environment.getProperty(APPLICATION_NAME)).orElse("default");
        String contextPath = Optional.ofNullable(environment.getProperty(CONTEXT_PATH)).orElse("");
        int localPort = event.getWebServer().getPort();
        String profile = StringUtils.arrayToCommaDelimitedString(environment.getActiveProfiles());
        String startInfo = String.format("[%s]---启动完成，当前使用的端口:[%d]，环境变量:[%s]", appName, localPort, profile);
        String url;

        // 如果有 swagger，打印开发阶段的 swagger ui 地址
        // noinspection AlibabaUndefineMagicConstant
        if (this.hasKnife4jUi()) {
            url = String.format("http://localhost:%s%s/doc.html", localPort, contextPath);
        } else if (this.hasSpringfoxSwaggerUi()) {
            url = String.format("http://localhost:%s%s/swagger-ui/index.html", localPort, contextPath);
        } else if (this.hasSwagger2Ui()) {
            url = String.format("http://localhost:%s%s/swagger-ui.html", localPort, contextPath);
        } else {
            url = String.format("http://localhost:%s%s", localPort, contextPath);
        }

        String bannerText = buildBannerText(startInfo, url);

        if (log.isInfoEnabled()) {
            log.info(bannerText);
        } else {
            System.out.print(bannerText);
        }

    }

    String buildBannerText(String startInfo, String url) {

        StringBuilder bannerTextBuilder = new StringBuilder();

        bannerTextBuilder
                .append(LINE_SEPARATOR)
                .append(LINE_SEPARATOR)
                .append(" :: Spring Boot (v").append(SpringBootVersion.getVersion()).append(") : ")
                .append(SPRING_BOOT_GITHUB_URL)
                .append(LINE_SEPARATOR)
                .append(" :: Bubble Project (v").append(Version.getVersion()).append(") : ")
                .append(BUBBLE_FIREWORKS_GITHUB_URL)
                .append(LINE_SEPARATOR)
                .append(" :: 启动信息 : (").append(startInfo).append(")")
                .append(LINE_SEPARATOR)
                .append(" :: 访问 : ").append(url)
                .append(LINE_SEPARATOR)
        ;
        return bannerTextBuilder.toString();
    }


    /**
     * hasKnife4jUi
     *
     * <p>
     *     是否包含 knife4j-spring-ui
     * </p>
     *
     * @since 2022/3/17 19:16
     * @return {@link boolean}
     */
    private boolean hasKnife4jUi() {
        try {
            ClassPathResource resource = new ClassPathResource("META-INF/resources/doc.html");
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * hasSpringfoxSwaggerUi
     *
     * <p>
     *     是否包含 springfox-swagger-ui
     * </p>
     *
     * @since 2022/3/17 19:37
     * @return {@link boolean}
     */
    private boolean hasSpringfoxSwaggerUi() {
        try {
            ClassPathResource resource = new ClassPathResource("META-INF/resources/webjars/springfox-swagger-ui/index.html");
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * hasSwagger2Ui
     *
     * <p>
     *     是否包含 springfox-swagger-ui
     * </p>
     *
     * @since 2022/3/17 19:33
     * @return {@link boolean}
     */
    private boolean hasSwagger2Ui() {
        try {
            ClassPathResource resource = new ClassPathResource("META-INF/resources/swagger-ui.html");
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

}
