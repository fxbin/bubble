package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

/**
 * UriUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 17:06
 */
@UtilityClass
public class UrlUtils extends org.springframework.web.util.UriUtils {

    /**
     * encode
     *
     * @author fxbin
     * @since 2020/3/23 17:08
     * @return java.lang.String
     */
    public static String encode(String source) {
        return encode(source, StandardCharsets.UTF_8);
    }

    /**
     * decode
     *
     * @author fxbin
     * @since 2020/3/23 17:08
     * @return java.lang.String
     */
    public static String decode(String source) {
        return decode(source, StandardCharsets.UTF_8);
    }

}
