package cn.fxbin.bubble.plugin.logging.exception;

import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * LoggingException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 13:51
 */
public class LoggingException extends RuntimeException {
    
    private static final long serialVersionUID = 2857970996604031794L;

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public LoggingException(String errmsg) {
            super(errmsg);
            this.errcode = -1;
            this.errmsg = errmsg;
    }

    public LoggingException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public LoggingException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public LoggingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggingException(Throwable cause) {
        super(cause);
    }

    protected LoggingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
