package cn.fxbin.bubble.ai.util;

import cn.hutool.core.util.StrUtil;

/**
 * 敏感数据工具类
 * <p>用于脱敏处理API Key、URL等敏感信息</p>
 *
 * @author fxbin
 */
public class SensitiveDataUtils {

    private static final int MASK_LENGTH = 8;
    private static final String MASK = "****";
    private static final int VISIBLE_PREFIX_LENGTH = 4;
    private static final int VISIBLE_SUFFIX_LENGTH = 4;
    private static final int SHORT_API_KEY_THRESHOLD = 12;

    /**
     * 脱敏 API Key
     *
     * @param apiKey API Key
     * @return 脱敏后的 API Key
     */
    public static String maskApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey)) {
            return MASK;
        }
        if (apiKey.length() <= SHORT_API_KEY_THRESHOLD) {
            return MASK;
        }
        return apiKey.substring(0, VISIBLE_PREFIX_LENGTH) + MASK + 
               apiKey.substring(apiKey.length() - VISIBLE_SUFFIX_LENGTH);
    }

    /**
     * 脱敏 URL
     *
     * @param url URL
     * @return 脱敏后的 URL
     */
    public static String maskUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            if (host != null) {
                String[] parts = host.split("\\.");
                if (parts.length >= 2) {
                    parts[0] = "****";
                    return uri.getScheme() + "://" + String.join(".", parts) + 
                           (uri.getPath() != null ? uri.getPath() : "");
                }
            }
        } catch (Exception e) {
            return url.substring(0, Math.min(url.length(), 20)) + MASK;
        }
        return url.substring(0, Math.min(url.length(), 20)) + MASK;
    }
}
