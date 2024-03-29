package cn.fxbin.bubble.core.exception;

import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import cn.fxbin.bubble.core.dataobject.Result;
import cn.fxbin.bubble.core.dataobject.ErrorCode;
import lombok.Getter;

/**
 * ServiceException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 11:05
 */
@Getter
public class ServiceException extends RuntimeException {

    private int errcode;

    private String errmsg;

    public ServiceException(String errmsg) {
        super(errmsg);
        this.errcode = -1;
        this.errmsg = errmsg;
    }

    public ServiceException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public ServiceException(Result<?> result) {
        super(result.getErrmsg());
        this.errcode = result.getErrcode();
        this.errmsg = result.getErrmsg();
    }

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.reasonPhrase());
        this.errcode = errorCode.value();
        this.errmsg = errorCode.reasonPhrase();
    }

    public ServiceException(ErrorCode errorCode, String errmsg, Object... args) {
        super(LoggerMessageFormat.format(errmsg, args));
        this.errcode = errorCode.value();
        this.errmsg = LoggerMessageFormat.format(errmsg, args);
    }

    public ServiceException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    protected ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
