package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.autoconfigure.DorisAutoConfiguration;
import cn.fxbin.bubble.data.doris.autoconfigure.DorisProperties;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

/**
 * BaseDorisIntegrationTest
 * <p>
 * Abstract base class for Doris integration tests using Testcontainers.
 * It starts a Doris cluster (1 FE, 1 BE) using Docker Compose.
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/29 10:00
 */
@Testcontainers
@SpringBootTest(classes = BaseDorisIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
public abstract class BaseDorisIntegrationTest {

    private static final String DORIS_FE_SERVICE_NAME = "doris-fe_1";
    private static final int DORIS_FE_HTTP_PORT = 8030;
    private static final int DORIS_FE_QUERY_PORT = 9030;
    private static final String DORIS_BE_SERVICE_NAME = "doris-be_1";
    private static final int DORIS_BE_HEARTBEAT_PORT = 9050;

    // Note: Using DockerComposeContainer as DorisContainer might not be fully mature or cover multi-node setups easily.
    // This approach requires a docker-compose.yml file.
    @Container
    public static DockerComposeContainer<?> dorisCluster = 
        new DockerComposeContainer<>(new File("src/test/resources/docker-compose-doris.yml"))
            .withExposedService(DORIS_FE_SERVICE_NAME, DORIS_FE_HTTP_PORT, Wait.forHttp("/api/health").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(5)))
            .withExposedService(DORIS_FE_SERVICE_NAME, DORIS_FE_QUERY_PORT, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
            .withExposedService(DORIS_BE_SERVICE_NAME, DORIS_BE_HEARTBEAT_PORT, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
            .withLocalCompose(true);

    @DynamicPropertySource
    static void dorisProperties(DynamicPropertyRegistry registry) {
        String feHost = dorisCluster.getServiceHost(DORIS_FE_SERVICE_NAME, DORIS_FE_HTTP_PORT);
        Integer feHttpPort = dorisCluster.getServicePort(DORIS_FE_SERVICE_NAME, DORIS_FE_HTTP_PORT);
        Integer feQueryPort = dorisCluster.getServicePort(DORIS_FE_SERVICE_NAME, DORIS_FE_QUERY_PORT);

        registry.add("bubble.data.doris.feHost", () -> feHost);
        registry.add("bubble.data.doris.feHttpPort", () -> feHttpPort);
        registry.add("bubble.data.doris.feQueryPort", () -> feQueryPort);
        registry.add("bubble.data.doris.username", () -> "root");
        registry.add("bubble.data.doris.password", () -> "");
        registry.add("bubble.data.doris.database", () -> "test_db"); // Default test database
        // Ensure Forest client points to the test container
        registry.add("forest.backend", () -> "okhttp3"); // Or httpclient
        registry.add("forest.variables.doris_fe_host", () -> feHost);
        registry.add("forest.variables.doris_fe_http_port", () -> feHttpPort);
    }

    @BeforeAll
    static void setup() {
        // You can perform initial setup here if needed, e.g., creating a test database
        // However, it's often better to do this in individual test methods or @BeforeEach
        // to ensure test isolation.
    }


    @Configuration
    @EnableAutoConfiguration // This will pick up DorisAutoConfiguration
    static class TestConfig {
        // You can define additional test-specific beans here if necessary
    }

}