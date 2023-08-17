package cn.fxbin.bubble.plugin.token.exception;

import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * TokenExpiredException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/14 18:03
 */
public class TokenExpiredException extends RuntimeException {
    
    private static final long serialVersionUID = -2332883400532039360L;

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public TokenExpiredException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenExpiredException(Throwable cause) {
        super(cause);
    }

    protected TokenExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
