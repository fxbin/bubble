package cn.fxbin.bubble.ai.lightrag.client;

import cn.fxbin.bubble.ai.lightrag.consts.ApiConst;
import cn.fxbin.bubble.ai.lightrag.consts.ApiConst.Endpoints;
import cn.fxbin.bubble.ai.lightrag.model.*;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Headers;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;

/**
 * LightRagDefaultClient
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/25 11:31
 */
@Headers({
        ApiConst.Headers.ACCEPT_JSON
})
public interface LightRagDefaultClient extends LightRagBaseClient {


    /**
     * 健康检查
     *
     * <p>检查查询服务的健康状态，包括服务可用性、
     * 响应时间、处理能力等。</p>
     *
     * @return 健康检查结果
     */
    @Get(Endpoints.DEFAULT_HEALTH)
    HealthCheckResponse healthCheck();

    /**
     * 获取认证状态
     *
     * <p>获取当前系统的认证状态信息，包括是否启用认证、
     * 当前认证状态以及访客令牌等信息。</p>
     *
     * @return 认证状态信息
     */
    @Get(Endpoints.DEFAULT_AUTH_STATUS)
    AuthStatusResponse getAuthStatus();

    /**
     * 用户登录
     *
     * <p>使用用户名和密码进行身份认证，成功后返回访问令牌
     * 和相关的认证信息。</p>
     *
     * @param loginRequest 登录请求参数，包含用户名、密码等信息
     * @return 登录结果，包含访问令牌等认证信息
     */
    @Post(value = Endpoints.DEFAULT_LOGIN, headers = ApiConst.Headers.CONTENT_TYPE_FORM_URLENCODED)
    LoginResponse login(@JSONBody LoginRequest loginRequest);

}
