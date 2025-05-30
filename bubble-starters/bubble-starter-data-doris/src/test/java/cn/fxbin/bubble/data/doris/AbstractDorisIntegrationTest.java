package cn.fxbin.bubble.data.doris;

import cn.fxbin.bubble.data.doris.autoconfigure.DorisAutoConfiguration;
import cn.fxbin.bubble.data.doris.autoconfigure.DorisProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Map;

/**
 * AbstractDorisIntegrationTest
 * Doris 集成测试基类，使用 Testcontainers 启动一个 Doris 容器作为测试环境。
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDorisIntegrationTest {

    private static final int DORIS_FE_QUERY_PORT = 9030; // Doris FE 查询端口



    @Container
    protected GenericContainer<?> dorisFeContainer =
            new GenericContainer<>(DockerImageName.parse("apache/doris:fe-3.0.5"))
                    .withExposedPorts(DORIS_FE_QUERY_PORT, 8030)
                    .withEnv("ENABLE_MULTIPLE_INSTANCES", "true")
                    // 设置空密码
                    .withEnv("ADMIN_PASSWD", "")
                    .withEnv("FE_ID", "1")
                    .withEnv("FE_SERVERS", "fe_1:127.0.0.1:9010")
                    .withEnv("FE_ID", "1")
                    .withEnv("MASTER_FE_IP", "127.0.0.1")
                    .withEnv("EDIT_LOG_PORT", "9010")  // 显式声明编辑日志端口
                    .withCreateContainerCmdModifier(cmd ->
                            cmd.getHostConfig()
                                            .withMemory(4294967296L)  // 4GB RAM
                                            .withCpuCount(2L).withStorageOpt(Map.of("size", "2G")))
                    .withLogConsumer(new Slf4jLogConsumer(log))
                    .waitingFor(Wait.forLogMessage(".*FE started.*", 1))
                    .withStartupTimeout(Duration.ofMinutes(3));
//                    .waitingFor(
//                            Wait.forLogMessage(".*BE successfully registered to FE.*", 1)
//                                    .withStartupTimeout(Duration.ofMinutes(10))
//                    );
    @Container
    protected GenericContainer<?> dorisBeContainer = new GenericContainer<>("apache/doris:be-3.0.5")
//            .withNetwork(dorisFeContainer.getNetwork())  // 共享同一网络
            .withEnv("BE_PORT", "9060")  // BE服务端口

//            .withEnv("FE_HOST", dorisFeContainer.getHost())  // 动态获取FE容器IP:ml-citation{ref="1,2" data="citationList"}
//            .withEnv("FE_QUERY_PORT", String.valueOf(dorisFeContainer.getMappedPort(9030)))  // FE查询端口
            .withEnv("BE_STORAGE_PATH", "/opt/doris/be/storage")  // 存储路径
            .withFileSystemBind("./be-storage", "/opt/doris/be/storage")  // 挂载本地存储目录:ml-citation{ref="8" data="citationList"}

            .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig()
                    .withMemory(4294967296L)  // 4GB内存
                    .withCpuCount(2L)
                    .withStorageOpt(Map.of("size", "2G")))
            .waitingFor(Wait.forLogMessage(".*BE start successfully.*", 1));


    protected DataSource dorisDataSource;
    protected JdbcTemplate dorisJdbcTemplate;
    protected NamedParameterJdbcTemplate dorisNamedParameterJdbcTemplate;
    protected DorisDdlOperations dorisDdlOperations;
    protected DorisTableDdlOperations dorisTableDdlOperations;
    protected DorisTableAutoPartition dorisTableAutoPartition;
    protected DorisProperties dorisProperties;

    @BeforeAll
    void setup() {
        dorisFeContainer.start();

        // 2. 动态设置BE容器的FE连接参数
        dorisBeContainer
                .withNetwork(dorisFeContainer.getNetwork())
                .withEnv("FE_SERVERS", "fe1:" + dorisFeContainer.getHost() + ":9010")
                .withEnv("FE_HOST", dorisFeContainer.getHost())
                .withEnv("FE_QUERY_PORT",
                        String.valueOf(dorisFeContainer.getMappedPort(9030)));

        dorisBeContainer.start();

        dorisProperties = new DorisProperties();
        dorisProperties.setEnabled(true);
        dorisProperties.setUrl(String.format("jdbc:mysql://%s:%d/",
                dorisFeContainer.getHost(), 9030));
        dorisProperties.setUsername("root");
        dorisProperties.setPassword(""); // Doris 默认 root 密码为空
        dorisProperties.setMaxActive(10);
        DorisProperties.AutoPartition autoPartitionProps = new DorisProperties.AutoPartition();
        autoPartitionProps.setEnabled(true);
        dorisProperties.setAutoPartition(autoPartitionProps);

        DorisAutoConfiguration autoConfiguration = new DorisAutoConfiguration(dorisProperties);

        dorisDataSource = autoConfiguration.dorisDataSource();
        dorisJdbcTemplate = autoConfiguration.dorisJdbcTemplate(dorisDataSource);
        dorisNamedParameterJdbcTemplate = autoConfiguration.dorisNamedParameterJdbcTemplate(dorisDataSource);

        JdbcDorisDdlOperations jdbcDorisDdlOperations = new JdbcDorisDdlOperations(dorisNamedParameterJdbcTemplate);
        dorisDdlOperations = jdbcDorisDdlOperations;
        dorisTableDdlOperations = jdbcDorisDdlOperations;
        dorisTableAutoPartition = autoConfiguration.dorisTableAutoPartition(dorisDdlOperations);

        // 确保数据库存在，增加重试机制
        int maxRetries = 3;
        int retryIntervalSeconds = 5;
        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                dorisJdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS test_db");
                dorisJdbcTemplate.execute("USE test_db");
                return;
            } catch (Exception e) {
                lastException = e;
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryIntervalSeconds * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("等待重试时被中断", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("无法创建或连接到测试数据库，重试次数：" + maxRetries, lastException);
    }

    @AfterAll
    void tearDown() {
        if (dorisFeContainer != null && dorisFeContainer.isRunning()) {
            dorisFeContainer.stop();
        }
        if (dorisBeContainer != null && dorisBeContainer.isRunning()) {
            dorisBeContainer.stop();
        }
    }
}