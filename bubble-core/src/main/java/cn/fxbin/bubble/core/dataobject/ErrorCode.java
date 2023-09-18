package cn.fxbin.bubble.core.dataobject;


import java.io.Serializable;
import java.util.ServiceLoader;

/**
 * 错误代码
 * ErrorCode
 *
 * <p>x
 * 分为两类：
 * 全局错误码（参照 {@link org.springframework.http.HttpStatus} 实现），实现为： {@code GlobalErrorCode} <br/>
 * 业务错误码  {@code BizErrorCode} <br/><br/>
 * <p>
 * 参照：<a href="https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status">HTTP 响应状态码</a>
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/08/29 00:44
 */

public sealed interface ErrorCode extends Serializable permits GlobalErrorCode, BizErrorCode {

    /**
     * Return the integer value of this status code.
     */
    int value();

    /**
     * Return the String value of error reason phrase.
     */
    String reasonPhrase();

    /**
     * Whether this status code is in the Informational class ({@code 1xx}).
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.1">RFC 2616</a>
     */
    default boolean is1xxInformational() {
        return false;
    };

    /**
     * Whether this status code is in the Successful class ({@code 2xx}).
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.2">RFC 2616</a>
     */
    default boolean is2xxSuccessful() {
        return false;
    }

    /**
     * Whether this status code is in the Redirection class ({@code 3xx}).
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.3">RFC 2616</a>
     */
    default boolean is3xxRedirection() {
        return false;
    }

    /**
     * Whether this status code is in the Client Error class ({@code 4xx}).
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.4">RFC 2616</a>
     */
    default boolean is4xxClientError() {
        return false;
    }

    /**
     * Whether this status code is in the Server Error class ({@code 5xx}).
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.5">RFC 2616</a>
     */
    default boolean is5xxServerError() {
        return false;
    }

    /**
     * Whether this status code is in the Client or Server Error class
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.4">RFC 2616</a>
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-10.3">RFC 2616</a>
     * ({@code 4xx} or {@code 5xx}).
     * @see #is4xxClientError()
     * @see #is5xxServerError()
     */
    default boolean isError() {
        return true;
    };

    default boolean isSameCodeAs(ErrorCode other) {
        return value() == other.value();
    }

    /**
     * 值
     *
     * 这是一种示例，关于BizErrorCodee 实现的
     *
     * @AutoService(BizErrorCode.class)
     * public class AppErrorCode implements BizErrorCode {
     *  public static final AppErrorCode USERNAME_FORMAT_ERROR = new AppErrorCode(1000000, "用户名格式有误");
     *  private int value;
     *  private String reasonPhrase;
     *  public AppErrorCode() {}
     *  public AppErrorCode(int value, String reasonPhrase) {
     *       this.value = value;
     *       this.reasonPhrase = reasonPhrase;
     *  }
     *  @Override
     *  public int value() {
     *       return this.value;
     *  }
     *   @Override
     *  public String reasonPhrase() {
     *       return this.reasonPhrase;
     *  }
     *  public BizErrorCode resolve(int errorCode) {
     *           return Arrays.stream(ReflectUtil.getFields(AppErrorCode.class))
     *                   .filter(field -> !"value".equals(field.getName()) && !"reasonPhrase".equals(field.getName()))
     *                   .filter(field -> ((AppErrorCode) ReflectUtil.getStaticFieldValue(field)).value == errorCode)
     *                   .map(field -> ((AppErrorCode) ReflectUtil.getStaticFieldValue(field)))
     *                   .findAny().orElse(null);
     *  }
     * }
     *
     * @param code code
     * @return {@link ErrorCode}
     */
    static ErrorCode valueOf(int code) {
        GlobalErrorCode errorCode = GlobalErrorCode.resolve(code);
        if (errorCode != null) {
            return errorCode;
        }

        // BizErrorCode 实现类的一种可行的示例：
        //
        ServiceLoader<BizErrorCode> errCodeLoader = ServiceLoader.load(BizErrorCode.class);
        for (BizErrorCode codeLoader : errCodeLoader) {
            BizErrorCode bizErrorCode = codeLoader.resolve(code);
            if (bizErrorCode.value() == code) {
                return bizErrorCode;
            }
        }

        return null;
    }

}
