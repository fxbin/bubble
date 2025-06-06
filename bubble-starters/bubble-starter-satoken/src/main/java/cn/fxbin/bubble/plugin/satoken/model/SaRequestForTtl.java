package cn.fxbin.bubble.plugin.satoken.model;

import cn.dev33.satoken.context.model.SaRequest;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SaRequestForTtl
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/5 9:53
 */
public class SaRequestForTtl implements SaRequest {
    private final TransmittableThreadLocal<String> tokenStorage = new TransmittableThreadLocal<>();
    private final TransmittableThreadLocal<Map<String, String>> paramStorage = TransmittableThreadLocal.withInitial(HashMap::new);

    @Override
    public Object getSource() {
        return null;
    }

    @Override
    public String getParam(String name) {
        return paramStorage.get().get(name);
    }

    @Override
    public List<String> getParamNames() {
        return new java.util.ArrayList<>(paramStorage.get().keySet());
    }

    @Override
    public Map<String, String> getParamMap() {
        return paramStorage.get();
    }

    @Override
    public String getHeader(String name) {
        if ("satoken".equals(name)) {
            return tokenStorage.get();
        }
        return null;
    }

    @Override
    public String getCookieValue(String name) {
        return "";
    }

    @Override
    public String getCookieFirstValue(String name) {
        return "";
    }

    @Override
    public String getCookieLastValue(String name) {
        return "";
    }

    @Override
    public String getRequestPath() {
        return "";
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String getMethod() {
        return "";
    }

    @Override
    public String getHost() {
        return "";
    }

    @Override
    public Object forward(String path) {
        return null;
    }

    public void setToken(String token) {
        tokenStorage.set(token);
    }

    public void clearToken() {
        tokenStorage.remove();
    }

    /**
     * 设置请求参数
     * @param name 参数名
     * @param value 参数值
     */
    public void setParam(String name, String value) {
        paramStorage.get().put(name, value);
    }

    /**
     * 清除所有ThreadLocal资源，防止内存泄漏
     */
    public void clear() {
        tokenStorage.remove();
        paramStorage.remove();
    }
}
