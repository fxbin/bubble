package cn.fxbin.bubble.ai.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityUtils 单元测试类
 * 
 * 测试安全工具类的各种脱敏功能，确保敏感信息能够被正确处理。
 * 涵盖API密钥、密码、URL和配置映射的脱敏测试场景。
 * 
 * @author fxbin
 * @since 2025-08-25
 */
@DisplayName("安全工具类测试")
class SecurityUtilsTest {

    @Test
    @DisplayName("测试API密钥脱敏 - 正常长度")
    void testMaskApiKey_NormalLength() {
        String apiKey = "sk-1234567890abcdef1234567890abcdef";
        String masked = SecurityUtils.maskApiKey(apiKey);
        
        assertNotNull(masked);
        assertTrue(masked.startsWith("sk-1"));
        assertTrue(masked.endsWith("cdef"));
        assertTrue(masked.contains("*"));
        assertNotEquals(apiKey, masked);
    }
    
    @Test
    @DisplayName("测试API密钥脱敏 - 短密钥")
    void testMaskApiKey_ShortKey() {
        String apiKey = "short";
        String masked = SecurityUtils.maskApiKey(apiKey);
        
        assertNotNull(masked);
        assertEquals("*****", masked);
    }
    
    @Test
    @DisplayName("测试API密钥脱敏 - 空值处理")
    void testMaskApiKey_NullAndEmpty() {
        assertNull(SecurityUtils.maskApiKey(null));
        assertEquals("", SecurityUtils.maskApiKey(""));
        assertEquals(" ", SecurityUtils.maskApiKey(" "));
    }
    
    @Test
    @DisplayName("测试密码脱敏")
    void testMaskPassword() {
        String password = "mySecretPassword123";
        String masked = SecurityUtils.maskPassword(password);
        
        assertNotNull(masked);
        assertEquals("********", masked);
        assertNotEquals(password, masked);
    }
    
    @Test
    @DisplayName("测试密码脱敏 - 短密码")
    void testMaskPassword_ShortPassword() {
        String password = "123";
        String masked = SecurityUtils.maskPassword(password);
        
        assertNotNull(masked);
        assertEquals("***", masked);
    }
    
    @Test
    @DisplayName("测试密码脱敏 - 空值处理")
    void testMaskPassword_NullAndEmpty() {
        assertNull(SecurityUtils.maskPassword(null));
        assertEquals("", SecurityUtils.maskPassword(""));
    }
    
    @Test
    @DisplayName("测试URL脱敏")
    void testMaskUrl() {
        String url = "https://user:password@example.com/api";
        String masked = SecurityUtils.maskUrl(url);
        
        assertNotNull(masked);
        assertEquals("https://***:***@example.com/api", masked);
    }
    
    @Test
    @DisplayName("测试URL脱敏 - 无认证信息")
    void testMaskUrl_NoAuth() {
        String url = "https://example.com/api";
        String masked = SecurityUtils.maskUrl(url);
        
        assertNotNull(masked);
        assertEquals(url, masked);
    }
    
    @Test
    @DisplayName("测试URL脱敏 - 空值处理")
    void testMaskUrl_NullAndEmpty() {
        assertNull(SecurityUtils.maskUrl(null));
        assertEquals("", SecurityUtils.maskUrl(""));
    }
    
    @Test
    @DisplayName("测试敏感配置脱敏 - 包含各种敏感字段")
    void testMaskSensitiveConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", "sk-1234567890abcdef");
        config.put("password", "myPassword123");
        config.put("baseUrl", "https://user:pass@api.example.com");
        config.put("timeout", "30s");
        config.put("maxRetries", 3);
        config.put("secretToken", "secret123456789");
        
        Map<String, Object> masked = SecurityUtils.maskSensitiveConfig(config);
        
        assertNotNull(masked);
        assertEquals(6, masked.size());
        
        // 验证敏感字段被脱敏
        assertNotEquals(config.get("apiKey"), masked.get("apiKey"));
        assertNotEquals(config.get("password"), masked.get("password"));
        assertNotEquals(config.get("secretToken"), masked.get("secretToken"));
        
        // 验证非敏感字段保持不变
        assertEquals(config.get("timeout"), masked.get("timeout"));
        assertEquals(config.get("maxRetries"), masked.get("maxRetries"));
        
        // 验证脱敏结果
        assertTrue(masked.get("apiKey").toString().contains("*"));
        assertEquals("********", masked.get("password"));
        assertTrue(masked.get("secretToken").toString().contains("*"));
    }
    
    @Test
    @DisplayName("测试敏感配置脱敏 - 空配置")
    void testMaskSensitiveConfig_EmptyConfig() {
        Map<String, Object> emptyConfig = new HashMap<>();
        Map<String, Object> masked = SecurityUtils.maskSensitiveConfig(emptyConfig);
        
        assertNotNull(masked);
        assertTrue(masked.isEmpty());
    }
    
    @Test
    @DisplayName("测试敏感配置脱敏 - null值处理")
    void testMaskSensitiveConfig_NullValues() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", null);
        config.put("normalField", "value");
        
        Map<String, Object> masked = SecurityUtils.maskSensitiveConfig(config);
        
        assertNotNull(masked);
        assertNull(masked.get("apiKey"));
        assertEquals("value", masked.get("normalField"));
    }
    
    @Test
    @DisplayName("测试敏感配置脱敏 - 大小写不敏感")
    void testMaskSensitiveConfig_CaseInsensitive() {
        Map<String, Object> config = new HashMap<>();
        config.put("API_KEY", "test-key");
        config.put("Password", "test-password");
        config.put("SECRET_TOKEN", "test-secret");
        
        Map<String, Object> masked = SecurityUtils.maskSensitiveConfig(config);
        
        assertNotNull(masked);
        assertNotEquals(config.get("API_KEY"), masked.get("API_KEY"));
        assertNotEquals(config.get("Password"), masked.get("Password"));
        assertNotEquals(config.get("SECRET_TOKEN"), masked.get("SECRET_TOKEN"));
    }
    
    @Test
    @DisplayName("测试敏感配置脱敏 - 复合字段名")
    void testMaskSensitiveConfig_CompositeFieldNames() {
        Map<String, Object> config = new HashMap<>();
        config.put("databasePassword", "db-pass");
        config.put("jwtSecretKey", "jwt-secret");
        config.put("oauthClientSecret", "oauth-secret");
        config.put("regularField", "normal-value");
        
        Map<String, Object> masked = SecurityUtils.maskSensitiveConfig(config);
        
        assertNotNull(masked);
        
        // 验证复合敏感字段被正确识别和脱敏
        assertNotEquals(config.get("databasePassword"), masked.get("databasePassword"));
        assertNotEquals(config.get("jwtSecretKey"), masked.get("jwtSecretKey"));
        assertNotEquals(config.get("oauthClientSecret"), masked.get("oauthClientSecret"));
        
        // 验证普通字段不受影响
        assertEquals(config.get("regularField"), masked.get("regularField"));
    }
}