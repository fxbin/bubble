package cn.fxbin.bubble.data.elasticsearch.exception;

import cn.fxbin.bubble.core.logging.LoggerMessageFormat;
import lombok.Getter;

/**
 * ElasticsearchException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/7 16:50
 */
public class ElasticsearchException extends RuntimeException {

    private static final long serialVersionUID = -8858817180797916781L;

    @Getter
    private int errcode;

    @Getter
    private String errmsg;

    public ElasticsearchException(String errmsg) {
        super(errmsg);
        this.errcode = -1;
        this.errmsg = errmsg;
    }

    public ElasticsearchException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public ElasticsearchException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticsearchException(Throwable cause) {
        super(cause);
    }

    protected ElasticsearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
