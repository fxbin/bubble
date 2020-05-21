package cn.fxbin.bubble.fireworks.core.exception;

import cn.fxbin.bubble.fireworks.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * LockException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/13 17:17
 */
public class LockException extends RuntimeException {

    private static final long serialVersionUID = -8858817180797916781L;

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public LockException(String message) {
        super(message);
    }

    public LockException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public LockException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }

    protected LockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
