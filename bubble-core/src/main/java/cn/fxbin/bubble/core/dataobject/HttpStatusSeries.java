package cn.fxbin.bubble.core.dataobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * HttpStatusSeries
 *
 * <p>
 *     Enumeration of HTTP status series.
 *     <a href="https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status">HTTP 响应状态码归类</a>
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/8/29 00:31
 * @see org.springframework.http.HttpStatus.Series
 */
@Getter
@AllArgsConstructor
public enum HttpStatusSeries {

    INFORMATIONAL(1),
    SUCCESSFUL(2),
    REDIRECTION(3),
    CLIENT_ERROR(4),
    SERVER_ERROR(5);

    private final int value;

    /**
     * Return the integer value of this status series. Ranges from 1 to 5.
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the {@code Series} enum constant for the supplied status code.
     * @param statusCode the HTTP status code (potentially non-standard)
     * @return the {@code Series} enum constant for the supplied status code
     * @throws IllegalArgumentException if this enum has no corresponding constant
     */
    public static HttpStatusSeries valueOf(int statusCode) {
        HttpStatusSeries series = resolve(statusCode);
        if (series == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        }
        return series;
    }

    /**
     * Resolve the given status code to an {@code StatusCodeSeries}, if possible.
     * @param statusCode the HTTP status code (potentially non-standard)
     * @return the corresponding {@code Series}, or {@code null} if not found
     */
    @Nullable
    public static HttpStatusSeries resolve(int statusCode) {
        int seriesCode = statusCode / 100;
        for (HttpStatusSeries series : values()) {
            if (series.value == seriesCode) {
                return series;
            }
        }
        return null;
    }
    
}
