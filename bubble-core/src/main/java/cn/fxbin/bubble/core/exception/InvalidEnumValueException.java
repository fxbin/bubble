package cn.fxbin.bubble.core.exception;

import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * InvalidEnumValueException
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/10/26 14:34
 */
@Getter
public class InvalidEnumValueException extends ServiceException{

    private int errcode;

    private String errmsg;

    public InvalidEnumValueException() {
        this("无效枚举值");
    }

    public InvalidEnumValueException(String errmsg) {
        super(errmsg);
        this.errcode = -1;
        this.errmsg = errmsg;
    }

    public InvalidEnumValueException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public InvalidEnumValueException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public InvalidEnumValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEnumValueException(Throwable cause) {
        super(cause);
    }

    protected InvalidEnumValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
