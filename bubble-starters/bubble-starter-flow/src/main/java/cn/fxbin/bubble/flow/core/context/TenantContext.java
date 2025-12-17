package cn.fxbin.bubble.flow.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * TenantContext
 *
 * @author fxbin
 * @since 2025/12/16
 */
@UtilityClass
public class TenantContext {

    private static final TransmittableThreadLocal<String> TENANT_ID_HOLDER = new TransmittableThreadLocal<>();

    /**
     * Get current tenant id.
     *
     * @return tenant id
     */
    public static String getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * Set current tenant id.
     *
     * @param tenantId tenant id
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * Clear current tenant id.
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
    }

    /**
     * Get current tenant id safely.
     *
     * @return optional tenant id
     */
    public static Optional<String> getTenantIdSafe() {
        return Optional.ofNullable(getTenantId());
    }
}
