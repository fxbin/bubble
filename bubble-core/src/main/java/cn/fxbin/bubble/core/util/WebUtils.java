package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.constant.StringPool;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * WebUtils - 优化版本
 * 提供高性能的Web工具类，包含缓存优化和更好的异常处理
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 18:06
 */
@Slf4j
@UtilityClass
public class WebUtils extends org.springframework.web.util.WebUtils {

    private final String HTTP_RULE = "^http(s)?://.*";

    private final Pattern PATTERN = Pattern.compile(HTTP_RULE);
    
    // IP地址缓存，提升性能
    private final Map<String, String> IP_CACHE = new ConcurrentHashMap<>();

    private final String[] IP_HEADER_NAMES = new String[]{
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private final Predicate<String> IP_PREDICATE = (ip) ->
            StringUtils.isBlank(ip) || StringPool.UNKNOWN.equalsIgnoreCase(ip);


    /**
     * getRequest
     *
     * <p>
     *     get jakarta.servlet.http.HttpServletRequest instance
     * </p>
     *
     * @since 2020/4/13 17:32
     * @return jakarta.servlet.http.HttpServletRequest
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
     * @return {@link Map<String, String>}
     */
    public Map<String, String> getRequestHeaders() {
        return WebUtils.getRequestHeaders(WebUtils.getRequest());
    }

    /**
     * getRequestHeaders
     *
     * @since 2020/4/13 17:31
     * @param request http request instance
     * @return {@link java.util.Map<java.lang.String,java.lang.String>}
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
     * getRequestBody
     *
     * @param request http request instance
     * @return {@link String}
     */
    public static String getRequestBody(HttpServletRequest request) {
        try (final BufferedReader reader = request.getReader()) {
            return IoUtil.read(reader);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }


    /**
     * getResponse
     *
     * <p>
     *     get jakarta.servlet.http.HttpServletResponse instance
     * </p>
     *
     * @since 2020/4/13 17:31
     * @return jakarta.servlet.http.HttpServletResponse
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
     * @return {@link java.util.Map<java.lang.String,java.lang.String>}
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
    public String required(final HttpServletRequest req, final String key) {
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
    public String optional(final HttpServletRequest req, final String key, final String defaultValue) {
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
    private String resolveValue(String value, String encoding) {
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
     * @param request {@link HttpServletRequest}
     * @return accept encode
     */
    public String getAcceptEncoding(HttpServletRequest request) {
        String encode = StringUtils.defaultIfEmpty(request.getHeader("Accept-Charset"), StandardCharsets.UTF_8.name());
        encode = encode.contains(",") ? encode.substring(0, encode.indexOf(",")) : encode;
        return encode.contains(";") ? encode.substring(0, encode.indexOf(";")) : encode;
    }

    /**
     * 获取用户代理
     *
     * @return {@link String}
     */
    public String getUserAgent() {
        return WebUtils.getUserAgent(WebUtils.getRequest());
    }

    /**
     * Returns the value of the request header "user-agent" as a <code>String</code>.
     *
     * @param request HttpServletRequest
     * @return the value of the request header "user-agent", or the value of the request header "client-version" if the
     * request does not have a header of "user-agent".
     */
    public String getUserAgent(HttpServletRequest request) {
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
     * 获取ip地址
     *
     * @return {@link String}
     */
    public String getIpAddr() {
        return WebUtils.getIpAddr(WebUtils.getRequest());
    }

    /**
     * 获取ip - 优化版本，添加缓存和更好的异常处理
     *
     * @since 2020/5/19 17:43
     * @param request HttpServletRequest
     * @return {@code String}
     */
    public String getIpAddr(@Nullable HttpServletRequest request) {
        if (request == null) {
            return StringPool.EMPTY;
        }
        
        // 生成缓存key
        String cacheKey = generateIpCacheKey(request);
        
        // 先从缓存中获取
        String cachedIp = IP_CACHE.get(cacheKey);
        if (cachedIp != null) {
            return cachedIp;
        }
        
        String ip = null;
        
        // 依次检查各种IP头
        for (String ipHeader : IP_HEADER_NAMES) {
            ip = request.getHeader(ipHeader);
            if (!IP_PREDICATE.test(ip)) {
                break;
            }
        }
        
        // 如果从头部获取不到有效IP，则从RemoteAddr获取
        if (IP_PREDICATE.test(ip)) {
            ip = request.getRemoteAddr();
            if (StringPool.LOCALHOST.equals(ip) || StringPool.LOCALHOST_IPV6.equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.warn("Failed to get local host address", e);
                    ip = StringPool.UNKNOWN;
                }
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        String finalIp = StringUtils.isBlank(ip) ? StringPool.UNKNOWN : StringUtils.splitTrim(ip, StringPool.COMMA)[0];
        
        // 缓存结果（避免缓存过多，限制缓存大小）
        if (IP_CACHE.size() < 1000) {
            IP_CACHE.put(cacheKey, finalIp);
        }
        
        return finalIp;
    }
    
    /**
     * 生成IP缓存key
     */
    private String generateIpCacheKey(HttpServletRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // 使用主要的IP相关头部信息作为缓存key
        for (String ipHeader : IP_HEADER_NAMES) {
            String headerValue = request.getHeader(ipHeader);
            if (headerValue != null) {
                keyBuilder.append(ipHeader).append(":").append(headerValue).append(";");
            }
        }
        
        keyBuilder.append("remote:").append(request.getRemoteAddr());
        return keyBuilder.toString();
    }


    /**
     * 获取本机网卡地址 - 优化版本，添加更好的异常处理
     *
     * @since 2020/5/19 17:46
     * @return macAddress
     */
    public String getLocalMac() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ia);
            
            if (networkInterface == null) {
                log.warn("Cannot find network interface for local host");
                return StringPool.UNKNOWN;
            }
            
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = networkInterface.getHardwareAddress();
            
            if (mac == null || mac.length == 0) {
                log.warn("Cannot get hardware address from network interface");
                return StringPool.UNKNOWN;
            }
            
            // 下面代码是把mac地址拼装成String
            StringBuilder sb = new StringBuilder(mac.length * 2);
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? "0" + s : s);
            }
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            return sb.toString().toUpperCase().replaceAll("-", "");
        } catch (Exception e) {
            log.error("Failed to get local MAC address", e);
            throw new IllegalStateException("getLocalMAC error: " + e.getMessage(), e);
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
