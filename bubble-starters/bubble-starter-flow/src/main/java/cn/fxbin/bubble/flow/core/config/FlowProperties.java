package cn.fxbin.bubble.flow.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FlowProperties
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

}
