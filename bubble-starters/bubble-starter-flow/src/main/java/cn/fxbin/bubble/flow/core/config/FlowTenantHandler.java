package cn.fxbin.bubble.flow.core.config;

import cn.fxbin.bubble.flow.core.context.TenantContext;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * FlowTenantHandler
 *
 * @author fxbin
 * @since 2025/12/16
 */
@RequiredArgsConstructor
public class FlowTenantHandler implements TenantLineHandler {

    private final FlowProperties flowProperties;

    @Override
    public Expression getTenantId() {
        // Return current tenant id from context
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return new StringValue(""); // Or handle null appropriately
        }
        return new StringValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return flowProperties.getTenant().getColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // Default check
        if (flowProperties.getTenant().getIgnoreTables() == null) {
            return false;
        }
        Set<String> ignoreTables = new HashSet<>(Arrays.asList(flowProperties.getTenant().getIgnoreTables()));
        return ignoreTables.contains(tableName);
    }
}
