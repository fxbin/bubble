package cn.fxbin.bubble.plugin.token.exception;

import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * InvalidClaimException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/14 18:46
 */
public class InvalidClaimException extends RuntimeException {
    
    private static final long serialVersionUID = -3551174960047563088L;

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public InvalidClaimException(String message) {
        super(message);
    }

    public InvalidClaimException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public InvalidClaimException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public InvalidClaimException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidClaimException(Throwable cause) {
        super(cause);
    }

    protected InvalidClaimException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
