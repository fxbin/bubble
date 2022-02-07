package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.constant.StringPool;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * WebUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:06
 */
@UtilityClass
public class WebUtils extends org.springframework.web.util.WebUtils {

    private final String HTTP_RULE = "^http(s)?://.*";

    private final Pattern PATTERN = Pattern.compile(HTTP_RULE);

    private final String[] IP_HEADER_NAMES = new String[]{
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private final Predicate<String> IP_PREDICATE = (ip) -> StringUtils.isBlank(ip) || StringPool.UNKNOWN.equalsIgnoreCase(ip);


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
     * getRequestMethod
     *
     * @since 2022/2/7 10:59 AM
     * @return {@link String}
     */
    public String getRequestMethod() {
        return Optional.ofNullable(WebUtils.getRequest())
                .map(HttpServletRequest::getMethod)
                .orElse(null);
    }


    /**
     * getQueryString
     *
     * @since 2022/2/7 10:37 AM
     * @return {@link String}
     */
    public String getQueryString() {
        return Optional.ofNullable(WebUtils.getRequest())
                .map(HttpServletRequest::getQueryString)
                .orElse(null);
    }


    /**
     * getRequestUrl
     *
     * @since 2022/2/7 10:57 AM
     * @return {@link String}
     */
    public String getRequestUrl() {
        return Optional.ofNullable(WebUtils.getRequest())
                .map(HttpServletRequest::getServletPath)
                .orElse(null);
    }


    /**
     * getSession
     *
     * @since 2022/2/7 10:58 AM
     * @return {@link HttpSession}
     */
    public HttpSession getSession() {
        return Optional.ofNullable(WebUtils.getRequest())
                .map(HttpServletRequest::getSession)
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
     * getResponseHeaders
     *
     * @since 2020/4/13 17:17
     * @param response http response instance
     * @return java.util.Map<java.lang.String,java.lang.String>
     */
    public Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Assert.notNull(response, "response instance is null.");
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }


    /**
     * get target value from parameterMap, if not found will throw {@link IllegalArgumentException}.
     *
     * @param req {@link HttpServletRequest}
     * @param key key
     * @return value
     */
    public static String required(final HttpServletRequest req, final String key) {
        String value = req.getParameter(key);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Param '" + key + "' is required.");
        }
        String encoding = req.getParameter("encoding");
        return resolveValue(value, encoding);
    }


    /**
     * get target value from parameterMap, if not found will return default value.
     *
     * @param req          {@link HttpServletRequest}
     * @param key          key
     * @param defaultValue default value
     * @return value
     */
    public static String optional(final HttpServletRequest req, final String key, final String defaultValue) {
        if (!req.getParameterMap().containsKey(key) || req.getParameterMap().get(key)[0] == null) {
            return defaultValue;
        }
        String value = req.getParameter(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        String encoding = req.getParameter("encoding");
        return resolveValue(value, encoding);
    }


    /**
     * decode target value.
     *
     * @param value    value
     * @param encoding encode
     * @return Decoded data
     */
    private static String resolveValue(String value, String encoding) {
        if (StringUtils.isBlank(encoding)) {
            encoding = StandardCharsets.UTF_8.name();
        }
        try {
            value = new String(value.getBytes(StandardCharsets.UTF_8), encoding);
        } catch (UnsupportedEncodingException ignore) {
        }
        return value.trim();
    }


    /**
     * get accept encode from request.
     *
     * @param req {@link HttpServletRequest}
     * @return accept encode
     */
    public static String getAcceptEncoding(HttpServletRequest req) {
        String encode = StringUtils.defaultIfEmpty(req.getHeader("Accept-Charset"), StandardCharsets.UTF_8.name());
        encode = encode.contains(",") ? encode.substring(0, encode.indexOf(",")) : encode;
        return encode.contains(";") ? encode.substring(0, encode.indexOf(";")) : encode;
    }


    /**
     * Returns the value of the request header "user-agent" as a <code>String</code>.
     *
     * @param request HttpServletRequest
     * @return the value of the request header "user-agent", or the value of the request header "client-version" if the
     * request does not have a header of "user-agent".
     */
    public static String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (StringUtils.isBlank(userAgent)) {
            userAgent = StringUtils
                    .defaultIfEmpty(request.getHeader("Client-Version"), StringPool.EMPTY);
        }
        return userAgent;
    }


    /**
     * IPv4 地址校验
     *
     * @since 2020/5/19 17:46
     * @param ip ip地址
     * @return Boolean
     */
    public Boolean isIpAddress(String ip) {
        String regex = "(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))";
        return Pattern.compile(regex).matcher(ip).matches();
    }


    /**
     * 获取ip
     *
     * @since 2020/5/19 17:43
     * @param request HttpServletRequest
     * @return {@code String}
     */
    public String getIpAddr(@Nullable HttpServletRequest request) {
        if (request == null) {
            return StringPool.EMPTY;
        }
        String ip = null;
        for (String ipHeader : IP_HEADER_NAMES) {
            ip = request.getHeader(ipHeader);
            if (!IP_PREDICATE.test(ip)) {
                break;
            }
        }
        if (IP_PREDICATE.test(ip)) {
            ip = request.getRemoteAddr();
            if (StringPool.LOCALHOST.equals(ip)) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ip = Optional.ofNullable(inet).map(InetAddress::getHostAddress).orElse(StringPool.UNKNOWN);
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        return StringUtils.isBlank(ip) ? null : StringUtils.splitTrim(ip, StringPool.COMMA)[0];
    }


    /**
     * 获取本机网卡地址
     *
     * @since 2020/5/19 17:46
     * @return macAddress
     */
    public String getLocalMac() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            // 下面代码是把mac地址拼装成String
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            return sb.toString().toUpperCase().replaceAll("-", "");
        } catch (Exception e) {
            throw new IllegalStateException("getLocalMAC error");
        }
    }


    /**
     * http(s)校验, 是否为http(s)请求
     * @param url 请求url
     * @return true/false
     */
    public Boolean isHttp(String url){
        return PATTERN.matcher(url).matches();
    }


}
