package cn.fxbin.bubble.plugin.satoken.model;

import cn.dev33.satoken.context.model.SaResponse;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SaResponseForTtl
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/5 9:54
 */
public class SaResponseForTtl implements SaResponse {
    private final TransmittableThreadLocal<Map<String, String>> headers = TransmittableThreadLocal.withInitial(HashMap::new);
    private final TransmittableThreadLocal<Map<String, String>> cookies = TransmittableThreadLocal.withInitial(HashMap::new);
    private final TransmittableThreadLocal<String> tokenStorage = new TransmittableThreadLocal<>();

    @Override
    public Object redirect(String url) {
        return "redirect:" + url;
    }

    @Override
    public Object getSource() {
        return this.headers.get();
    }

    @Override
    public SaResponse setStatus(int sc) {
        return this;
    }

    @Override
    public SaResponse setHeader(String name, String value) {
        headers.get().put(name, value);
        if ("satoken".equals(name)) {
            tokenStorage.set(value);
        }
        return this;
    }

    @Override
    public SaResponse addHeader(String name, String value) {
        headers.get().put(name, value);
        if ("satoken".equals(name)) {
            tokenStorage.set(value);
        }
        return this;
    }

    public SaResponse setCookie(String name, String value, String path, String domain, int maxAge) {
        Map<String, String> cookieMap = new HashMap<>();
        cookieMap.put("path", path);
        cookieMap.put("domain", domain);
        cookieMap.put("maxAge", String.valueOf(maxAge));
        cookies.get().put(name, value + "##" + String.join("&", cookieMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"))));
        return this;
    }

    /**
     * 清除ThreadLocal资源，防止内存泄漏
     */
    public void clear() {
        headers.remove();
        cookies.remove();
        tokenStorage.remove();
    }
}
