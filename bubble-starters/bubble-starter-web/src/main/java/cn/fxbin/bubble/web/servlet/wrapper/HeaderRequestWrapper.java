package cn.fxbin.bubble.web.servlet.wrapper;

import com.google.common.collect.Maps;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * HeaderRequestWrapper
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/4/5 20:36
 */
public class HeaderRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> headerMap = Maps.newHashMap();

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */
    public HeaderRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public HeaderRequestWrapper addHeader(String name, String value) {
        headerMap.putIfAbsent(name, value);
        return this;
    }

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(headerMap.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }

}
