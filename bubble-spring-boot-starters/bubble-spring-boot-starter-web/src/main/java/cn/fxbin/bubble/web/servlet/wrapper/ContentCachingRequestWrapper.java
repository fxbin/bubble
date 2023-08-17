package cn.fxbin.bubble.web.servlet.wrapper;

import cn.fxbin.bubble.core.util.IoUtils;
import lombok.SneakyThrows;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * ContentCachingRequestWrapper
 *
 * <p>
 *     注意点： 表单提交的请求，会丢失请求数据，该包装类仅适用于 application/json 方式的数据，慎用
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/4/5 20:57
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {

    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */
    @SneakyThrows
    public ContentCachingRequestWrapper(HttpServletRequest request) {
        super(request);
        this.body = IoUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {}

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

        };
    }


}
