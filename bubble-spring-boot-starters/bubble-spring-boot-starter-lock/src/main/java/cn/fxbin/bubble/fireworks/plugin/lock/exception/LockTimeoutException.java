package cn.fxbin.bubble.fireworks.plugin.lock.exception;

import cn.fxbin.bubble.fireworks.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * LockTimeoutException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 15:10
 */
public class LockTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 5358365194033458115L;

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public LockTimeoutException(String errmsg) {
        super(errmsg);
        this.errcode = -1;
        this.errmsg = errmsg;
    }

    public LockTimeoutException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public LockTimeoutException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public LockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockTimeoutException(Throwable cause) {
        super(cause);
    }

    protected LockTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
