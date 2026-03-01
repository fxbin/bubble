package cn.fxbin.bubble.flow.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FlowProperties
 *
 * <p>流程引擎配置属性，包含启用开关、多租户配置、执行配置等。
 *
 * @author fxbin
 * @since 2025/12/16
 */
@Data
@ConfigurationProperties(prefix = "bubble.flow")
public class FlowProperties {

    /**
     * Whether to enable the flow starter.
     */
    private boolean enabled = true;

    /**
     * Multi-tenancy configuration.
     */
    private Tenant tenant = new Tenant();

    /**
     * Execution configuration.
     */
    private Execution execution = new Execution();

    @Data
    public static class Tenant {
        /**
         * Whether to enable multi-tenancy.
         */
        private boolean enabled = false;

        /**
         * The column name for tenant id.
         */
        private String column = "tenant_id";

        /**
         * Ignore tables for multi-tenancy.
         */
        private String[] ignoreTables = new String[]{};
    }

    @Data
    public static class Execution {
        /**
         * Maximum wait seconds for parallel node execution.
         */
        private int maxWaitSeconds = 900;

        /**
         * Whether to ignore error when parallel execution fails.
         */
        private boolean ignoreError = true;
    }

}
