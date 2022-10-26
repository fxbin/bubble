package cn.fxbin.bubble.fireworks.core.exception;

import cn.fxbin.bubble.fireworks.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * UtilException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 17:53
 */
public class UtilException extends ServiceException {

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public UtilException(String errmsg) {
        super(errmsg);
        this.errcode = -1;
        this.errmsg = errmsg;
    }

    public UtilException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public UtilException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public UtilException(String message, Throwable cause) {
        super(message, cause);
    }

    public UtilException(Throwable cause) {
        super(cause);
    }

    protected UtilException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}