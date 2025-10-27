package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.constant.StringPool;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * WebUtils 单元测试
 * 
 * <p>
 * 测试WebUtils类中的请求体读取功能，包括：
 * - 智能检测和缓存机制
 * - 重复读取保护
 * - 异常处理
 * - 边界条件处理
 * </p>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025/10/12 21:59
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebUtils 请求体读取测试")
class WebUtilsTest {

    @Mock
    private HttpServletRequest mockRequest;

    private static final String TEST_REQUEST_BODY = "{\"name\":\"test\",\"value\":\"data\"}";
    private static final String REQUEST_BODY_CACHE_KEY = "BUBBLE_REQUEST_BODY_CACHE";
    private static final String REQUEST_BODY_READ_STATUS_KEY = "BUBBLE_REQUEST_BODY_READ_STATUS";

    @BeforeEach
    void setUp() {
        // 重置mock对象
        reset(mockRequest);
    }

    @Test
    @DisplayName("测试正常读取请求体")
    void testGetRequestBody_Normal() throws IOException {
        // Given
        BufferedReader reader = new BufferedReader(new StringReader(TEST_REQUEST_BODY));
        when(mockRequest.getReader()).thenReturn(reader);
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(TEST_REQUEST_BODY.length());
        when(mockRequest.getContentType()).thenReturn("application/json");

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(TEST_REQUEST_BODY);
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, TEST_REQUEST_BODY);
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试从缓存中获取请求体")
    void testGetRequestBody_FromCache() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(TEST_REQUEST_BODY);

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(TEST_REQUEST_BODY);
        verify(mockRequest, never()).getReader();
        verify(mockRequest, never()).setAttribute(eq(REQUEST_BODY_CACHE_KEY), anyString());
    }

    @Test
    @DisplayName("测试请求体已被读取的情况")
    void testGetRequestBody_AlreadyRead() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(Boolean.TRUE);

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(StringPool.EMPTY);
        verify(mockRequest, never()).getReader();
    }

    @Test
    @DisplayName("测试请求对象为null的情况")
    void testGetRequestBody_NullRequest() {
        // When
        String result = WebUtils.getRequestBody(null);

        // Then
        assertThat(result).isEqualTo(StringPool.EMPTY);
    }

    @Test
    @DisplayName("测试请求体过大的情况")
    void testGetRequestBody_TooLarge() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(2 * 1024 * 1024); // 2MB

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo("[REQUEST_BODY_TOO_LARGE]");
        verify(mockRequest, never()).getReader();
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, "[REQUEST_BODY_TOO_LARGE]");
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试multipart/form-data类型的请求")
    void testGetRequestBody_MultipartData() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(100);
        when(mockRequest.getContentType()).thenReturn("multipart/form-data; boundary=something");

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo("[MULTIPART_DATA]");
        verify(mockRequest, never()).getReader();
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, "[MULTIPART_DATA]");
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试IllegalStateException异常处理")
    void testGetRequestBody_IllegalStateException() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(100);
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest.getReader()).thenThrow(new IllegalStateException("getInputStream() has already been called"));

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试IOException异常处理")
    void testGetRequestBody_IOException() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(100);
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest.getReader()).thenThrow(new IOException("Network error"));

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试其他未知异常处理")
    void testGetRequestBody_UnknownException() throws IOException {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(100);
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest.getReader()).thenThrow(new RuntimeException("Unknown error"));

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试重复调用getRequestBody")
    void testGetRequestBody_MultipleCallsWithCache() {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null)
                .thenReturn(TEST_REQUEST_BODY);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(TEST_REQUEST_BODY.length());
        when(mockRequest.getContentType()).thenReturn("application/json");
        
        try {
            BufferedReader reader = new BufferedReader(new StringReader(TEST_REQUEST_BODY));
            when(mockRequest.getReader()).thenReturn(reader);
        } catch (IOException e) {
            // 这里不会发生，因为StringReader不会抛出IOException
        }

        // When - 第一次调用
        String firstResult = WebUtils.getRequestBody(mockRequest);
        // When - 第二次调用
        String secondResult = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(firstResult).isEqualTo(TEST_REQUEST_BODY);
        assertThat(secondResult).isEqualTo(TEST_REQUEST_BODY);
        assertThat(firstResult).isEqualTo(secondResult);
    }

    @Test
    @DisplayName("测试空请求体的处理")
    void testGetRequestBody_EmptyBody() {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(0);
        when(mockRequest.getContentType()).thenReturn("application/json");
        
        try {
            BufferedReader reader = new BufferedReader(new StringReader(""));
            when(mockRequest.getReader()).thenReturn(reader);
        } catch (IOException e) {
            // 这里不会发生，因为StringReader不会抛出IOException
        }

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, StringPool.EMPTY);
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }

    @Test
    @DisplayName("测试Content-Length为-1的情况")
    void testGetRequestBody_UnknownContentLength() {
        // Given
        when(mockRequest.getAttribute(REQUEST_BODY_CACHE_KEY)).thenReturn(null);
        when(mockRequest.getAttribute(REQUEST_BODY_READ_STATUS_KEY)).thenReturn(null);
        when(mockRequest.getContentLength()).thenReturn(-1);
        when(mockRequest.getContentType()).thenReturn("application/json");
        
        try {
            BufferedReader reader = new BufferedReader(new StringReader(TEST_REQUEST_BODY));
            when(mockRequest.getReader()).thenReturn(reader);
        } catch (IOException e) {
            // 这里不会发生，因为StringReader不会抛出IOException
        }

        // When
        String result = WebUtils.getRequestBody(mockRequest);

        // Then
        assertThat(result).isEqualTo(TEST_REQUEST_BODY);
        verify(mockRequest).setAttribute(REQUEST_BODY_CACHE_KEY, TEST_REQUEST_BODY);
        verify(mockRequest).setAttribute(REQUEST_BODY_READ_STATUS_KEY, Boolean.TRUE);
    }
}