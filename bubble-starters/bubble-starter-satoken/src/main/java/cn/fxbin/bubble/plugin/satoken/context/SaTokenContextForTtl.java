package cn.fxbin.bubble.plugin.satoken.context;

import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.context.model.SaTokenContextModelBox;

/**
 * SaTokenContextForTtl
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/6 11:07
 */
public class SaTokenContextForTtl implements SaTokenContext {
    @Override
    public void setContext(SaRequest req, SaResponse res, SaStorage stg) {
        SaTokenContextForTtlStaff.setModelBox(req, res, stg);
    }

    @Override
    public void clearContext() {
        SaTokenContextForTtlStaff.clearModelBox();
    }

    @Override
    public boolean isValid() {
        return SaTokenContextForTtlStaff.getModelBox()  != null;
    }

    @Override
    public SaTokenContextModelBox getModelBox() {
        return SaTokenContextForTtlStaff.getModelBox();
    }
}

