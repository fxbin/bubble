package cn.fxbin.bubble.openfeign;

import lombok.experimental.UtilityClass;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp3ConnectionManager
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/22 16:26
 */
@UtilityClass
public class OkHttp3ConnectionManager {

    public OkHttpClient createDefault() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                // 设置连接超时时间
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                // 创建OkHttpClient设置管理Cookie的CookieJar, 自动携带，保存和更新Cookie
                .cookieJar(new CookieJar() {

                    private final Map<String, List<Cookie>> COOKIE_STORE = new HashMap<>();

                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                        COOKIE_STORE.put(httpUrl.host(), list);
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                        List<Cookie> cookies = COOKIE_STORE.get(httpUrl.host());
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .connectionPool(new ConnectionPool())
                .build();
    }

}
