package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * WebUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:06
 */
@UtilityClass
public class WebUtils extends org.springframework.web.util.WebUtils {


    /**
     * getRequest
     *
     * <p>
     *     get javax.servlet.http.HttpServletRequest instance
     * </p>
     *
     * @since 2020/4/13 17:32
     * @return javax.servlet.http.HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(x -> (ServletRequestAttributes) x)
                .map(ServletRequestAttributes::getRequest)
                .orElse(null);
    }


    /**
     * getResponse
     *
     * <p>
     *     get javax.servlet.http.HttpServletResponse instance
     * </p>
     *
     * @since 2020/4/13 17:31
     * @return javax.servlet.http.HttpServletResponse
     */
    public HttpServletResponse getResponse() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(x -> (ServletRequestAttributes) x)
                .map(ServletRequestAttributes::getResponse)
                .orElse(null);
    }


    /**
     * getRequestHeaders
     *
     * @since 2020/4/13 17:31
     * @param request http request instance
     * @return java.util.Map<java.lang.String,java.lang.String>
     */
    public Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Assert.notNull(request, "request instance is null.");
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }


    /**
     * getResponseHeaders
     *
     * @since 2020/4/13 17:17
     * @param response http response instance
     * @return java.util.Map<java.lang.String,java.lang.String>
     */
    public Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Assert.notNull(response, "response instance is null.");
        Map<String, String> headers = new HashMap<>();
        Iterator<String> iterator = response.getHeaderNames().iterator();
        while (iterator.hasNext()) {
            String headerName = iterator.next();
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }


}
