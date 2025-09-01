package cn.fxbin.bubble.ai.lightrag.util;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 安全工具类
 * 
 * 提供敏感信息脱敏处理功能，用于保护系统中的敏感数据不被泄露。
 * 主要用于日志记录、健康检查、监控等场景中对敏感信息进行安全处理。
 * 
 * 支持的脱敏类型：
 * - API密钥和令牌
 * - 密码和凭证
 * - 个人身份信息
 * - 数据库连接字符串
 * 
 * @author fxbin
 * @since 2025-08-25
 */
public final class SecurityUtils {

    /**
     * 默认脱敏字符
     */
    private static final String MASK_CHAR = "*";
    
    /**
     * API密钥最小显示长度
     */
    private static final int API_KEY_MIN_VISIBLE_LENGTH = 4;
    
    /**
     * 密码最大显示长度
     */
    private static final int PASSWORD_MAX_VISIBLE_LENGTH = 0;
    
    /**
     * 敏感字段名称模式
     */
    private static final Pattern SENSITIVE_FIELD_PATTERN = Pattern.compile(
        "(?i).*(password|pwd|secret|key|token|credential|auth).*"
    );
    
    /**
     * 私有构造函数，防止实例化
     */
    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 脱敏API密钥
     * 
     * 对API密钥进行脱敏处理，保留前后少量字符用于识别，中间部分用星号替换。
     * 如果密钥长度小于最小可见长度的两倍，则完全脱敏。
     * 
     * @param apiKey API密钥
     * @return 脱敏后的API密钥
     */
    public static String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return apiKey;
        }
        
        int length = apiKey.length();
        if (length <= API_KEY_MIN_VISIBLE_LENGTH * 2) {
            return MASK_CHAR.repeat(Math.min(length, 8));
        }
        
        String prefix = apiKey.substring(0, API_KEY_MIN_VISIBLE_LENGTH);
        String suffix = apiKey.substring(length - API_KEY_MIN_VISIBLE_LENGTH);
        int maskLength = length - API_KEY_MIN_VISIBLE_LENGTH * 2;
        
        return prefix + MASK_CHAR.repeat(Math.min(maskLength, 8)) + suffix;
    }
    
    /**
     * 脱敏密码
     * 
     * 对密码进行完全脱敏处理，不显示任何原始字符。
     * 
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String maskPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return password;
        }
        
        return MASK_CHAR.repeat(Math.min(password.length(), 8));
    }
    
    /**
     * 脱敏URL中的敏感信息
     * 
     * 对URL中可能包含的用户名、密码等敏感信息进行脱敏处理。
     * 
     * @param url 原始URL
     * @return 脱敏后的URL
     */
    public static String maskUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        
        // 简单的URL脱敏，主要处理用户名密码部分
        return url.replaceAll("://[^@/]+@", "://***:***@");
    }
    
    /**
     * 脱敏配置映射
     * 
     * 对配置信息映射中的敏感字段进行脱敏处理。
     * 自动识别包含敏感关键词的字段名，并对其值进行相应的脱敏处理。
     * 
     * @param configMap 原始配置映射
     * @return 脱敏后的配置映射
     */
    public static Map<String, Object> maskSensitiveConfig(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return configMap;
        }
        
        Map<String, Object> maskedMap = new HashMap<>(configMap.size());
        
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value == null) {
                maskedMap.put(key, null);
                continue;
            }
            
            String stringValue = value.toString();
            
            if (isSensitiveField(key)) {
                // 根据字段类型选择不同的脱敏策略
                if (isPasswordField(key)) {
                    maskedMap.put(key, maskPassword(stringValue));
                } else if (isApiKeyField(key)) {
                    maskedMap.put(key, maskApiKey(stringValue));
                } else if (isUrlField(key)) {
                    maskedMap.put(key, maskUrl(stringValue));
                } else {
                    // 默认使用API密钥脱敏策略
                    maskedMap.put(key, maskApiKey(stringValue));
                }
            } else {
                maskedMap.put(key, value);
            }
        }
        
        return maskedMap;
    }
    
    /**
     * 判断是否为敏感字段
     * 
     * @param fieldName 字段名称
     * @return 是否为敏感字段
     */
    private static boolean isSensitiveField(String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            return false;
        }
        
        return SENSITIVE_FIELD_PATTERN.matcher(fieldName).matches();
    }
    
    /**
     * 判断是否为密码字段
     * 
     * @param fieldName 字段名称
     * @return 是否为密码字段
     */
    private static boolean isPasswordField(String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("password") || lowerFieldName.contains("pwd");
    }
    
    /**
     * 判断是否为API密钥字段
     * 
     * @param fieldName 字段名称
     * @return 是否为API密钥字段
     */
    private static boolean isApiKeyField(String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("key") || lowerFieldName.contains("token") 
               || lowerFieldName.contains("secret");
    }
    
    /**
     * 判断是否为URL字段
     * 
     * @param fieldName 字段名称
     * @return 是否为URL字段
     */
    private static boolean isUrlField(String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("url") || lowerFieldName.contains("uri") 
               || lowerFieldName.contains("endpoint");
    }
}