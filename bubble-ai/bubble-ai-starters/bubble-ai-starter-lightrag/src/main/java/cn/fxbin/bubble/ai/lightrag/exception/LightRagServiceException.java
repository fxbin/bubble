package cn.fxbin.bubble.ai.lightrag.exception;

import cn.fxbin.bubble.core.dataobject.ErrorCode;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * LightRAG 服务异常类
 * 
 * <p>LightRAG 服务层的统一异常类，用于封装和传递服务处理过程中
 * 发生的各种异常情况。该异常类提供了丰富的构造方法和错误信息
 * 处理能力，支持异常链传递和详细的错误上下文信息。</p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
public class LightRagServiceException extends ServiceException {
    private int errcode;

    private String errmsg;

    public LightRagServiceException() {
        this("LightRAG 服务异常");
    }

    public LightRagServiceException(String errmsg) {
        super(errmsg);
        this.errcode = -1;
        this.errmsg = errmsg;
    }

    public LightRagServiceException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public LightRagServiceException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public LightRagServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightRagServiceException(Throwable cause) {
        super(cause);
    }

    protected LightRagServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}