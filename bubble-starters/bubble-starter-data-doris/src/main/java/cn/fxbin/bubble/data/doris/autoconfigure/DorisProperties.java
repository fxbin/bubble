package cn.fxbin.bubble.data.doris.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * DorisProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = DorisProperties.PREFIX)
public class DorisProperties {

    /**
     * 配置属性的前缀。
     */
    public static final String PREFIX = "bubble.doris";

    /**
     * 是否启用 Doris 自动配置。
     */
    private boolean enabled = false;

    /**
     * Doris 的 JDBC URL。
     */
    private String url;

    /**
     * Doris 连接用户名。
     */
    private String username;

    /**
     * Doris 连接密码。
     */
    private String password;

    /**
     * 连接池中最大活跃连接数。
     */
    private int maxActive = 20;

    /**
     * 从连接池获取连接的最大等待时间。
     */
    private Duration maxWait = Duration.ofSeconds(30);

    /**
     * 连接超时时间。
     */
    private Duration connectionTimeout = Duration.ofSeconds(30);

    /**
     * 是否启用自动分区功能。
     */
    private AutoPartition autoPartition = new AutoPartition();


    @Getter
    @Setter
    @ToString
    public static class AutoPartition {

        /**
         * 是否启用自动分区。
         */
        private boolean enabled = false;

        /**
         * 自动分区类型。
         * 支持：RANGE, LIST
         */
        private AutoPartitionType type;

        /**
         * 分区列或表达式。
         * 对于 RANGE 类型，例如：date_trunc(TRADE_DATE, 'month')
         * 对于 LIST 类型，例如：str_column 或 column1,column2
         */
        private String partitionBy;

        /**
         * RANGE 类型的分区粒度。
         * 支持：year, month, day, hour, minute
         * 仅适用于 RANGE 类型。
         */
        private String granularity;

        /**
         * 是否允许 LIST 分区列中包含 NULL 值。
         * 仅适用于 LIST 类型。
         */
        private boolean allowPartitionColumnNullable = false;

        /**
         * 是否启用动态分区创建（仅适用于自动分区）。
         * 最佳实践建议关闭动态分区的创建功能，仅保留其回收功能，以实现分区的灵活性创建与统一管理。
         */
        private boolean enableDynamicCreation = false;

    }

    public enum AutoPartitionType {
        RANGE, LIST
    }

}