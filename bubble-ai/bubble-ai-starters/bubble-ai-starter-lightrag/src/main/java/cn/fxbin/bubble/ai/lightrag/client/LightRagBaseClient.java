package cn.fxbin.bubble.ai.lightrag.client;

import cn.fxbin.bubble.ai.lightrag.autoconfigure.LightRagAutoConfiguration.LightRagAuthInterceptor;
import cn.fxbin.bubble.ai.lightrag.autoconfigure.LightRagAutoConfiguration.LightRagLoggingInterceptor;
import cn.fxbin.bubble.ai.lightrag.consts.ApiConst;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Headers;

/**
 * LightRagBaseClient
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 15:59
 */
@Headers({
        ApiConst.Headers.USER_AGENT
})
@BaseRequest(
        baseURL = ApiConst.BASE_URL,
        interceptor = {LightRagAuthInterceptor.class, LightRagLoggingInterceptor.class})
public interface LightRagBaseClient {
}
