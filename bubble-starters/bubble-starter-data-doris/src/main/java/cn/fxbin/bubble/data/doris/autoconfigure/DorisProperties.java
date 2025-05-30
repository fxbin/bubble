package cn.fxbin.bubble.data.doris.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DorisProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/7/11 17:35
 */
@Data
@ConfigurationProperties(prefix = "bubble.data.doris")
public class DorisProperties {

    /**
     * Doris FE host
     */
    private String feHost = "localhost";

    /**
     * Doris FE HTTP port
     */
    private Integer feHttpPort = 8030;

    /**
     * Doris FE query port (MySQL protocol)
     */
    private Integer feQueryPort = 9030;

    /**
     * Doris username
     */
    private String username = "root";

    /**
     * Doris password
     */
    private String password = "";

    /**
     * JDBC URL template
     */
    private String jdbcUrlTemplate = "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true&rewriteBatchedStatements=true";

    /**
     * Default database name
     */
    private String database;

    /**
     * Connection pool configuration
     */
    private Pool pool = new Pool();

    /**
     * StreamLoad configuration
     */
    private StreamLoad streamLoad = new StreamLoad();

    /**
     * Auto partition configuration
     */
    private AutoPartition autoPartition = new AutoPartition();

    @Data
    public static class Pool {
        private int maxPoolSize = 10;
        private int minIdle = 1;
        private long maxLifetime = 1800000; // 30 minutes
        private long connectionTimeout = 30000; // 30 seconds
        private long idleTimeout = 600000; // 10 minutes
    }

    @Data
    public static class StreamLoad {
        /**
         * Maximum retries for StreamLoad
         */
        private int maxRetries = 3;

        /**
         * Retry interval in milliseconds
         */
        private long retryIntervalMs = 1000;

        /**
         * Default timeout for StreamLoad in milliseconds
         */
        private long timeoutMs = 600000; // 10 minutes

        /**
         * Maximum rows per batch for StreamLoad
         */
        private int maxBatchRows = 100000;

        /**
         * Maximum size per batch for StreamLoad in bytes
         */
        private long maxBatchSizeBytes = 100 * 1024 * 1024; // 100MB

        /**
         * Default format for StreamLoad (json or csv)
         */
        private String defaultFormat = "json";

        /**
         * Whether to strip outer array for JSON format
         */
        private boolean stripOuterArray = false;

        /**
         * Column separator for CSV format
         */
        private String columnSeparator = ",";

        /**
         * Line delimiter for CSV format
         */
        private String lineDelimiter = "\n";
    }

    @Data
    public static class AutoPartition {
        /**
         * Whether to enable auto partition feature.
         */
        private boolean enabled = false;

        /**
         * Default partition type if not specified in DDL (e.g., RANGE, LIST).
         * This might be used if the system needs a fallback or global default.
         */
        private AutoPartitionType defaultType = AutoPartitionType.RANGE;

        /**
         * Default time unit for RANGE partitioning if not derived from DDL (e.g., MONTH, DAY).
         */
        private String defaultRangeTimeUnit = "MONTH";

        /**
         * Default number of future partitions to create for RANGE type.
         */
        private int defaultRangeFuturePartitions = 3;

        /**
         * Default prefix for partition names.
         */
        private String defaultPartitionPrefix = "p";
    }

    /**
     * Enum for Auto Partition Type
     */
    public enum AutoPartitionType {
        RANGE,
        LIST
    }

    /**
     * Get JDBC URL for the specified database
     *
     * @param database database name
     * @return JDBC URL
     */
    public String getJdbcUrl(String database) {
        return String.format(jdbcUrlTemplate, feHost, feQueryPort, database);
    }

    /**
     * Get JDBC URL for the default database
     *
     * @return JDBC URL
     */
    public String getJdbcUrl() {
        return getJdbcUrl(database);
    }
}