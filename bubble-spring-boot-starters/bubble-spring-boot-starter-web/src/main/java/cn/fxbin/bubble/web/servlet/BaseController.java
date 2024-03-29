package cn.fxbin.bubble.web.servlet;

import cn.fxbin.bubble.core.dataobject.GlobalErrorCode;
import cn.fxbin.bubble.core.dataobject.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

/**
 * BaseController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 16:07
 */
@Slf4j
public abstract class BaseController {

    @Resource
    public HttpServletRequest request;

    @Resource
    public HttpServletResponse response;

    /**
     * success
     *
     * @since 2020/3/31 10:59
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public <T> Result<T> success() {
        return Result.success();
    }

    /**
     * success
     *
     * @since 2020/3/31 10:59
     * @param data 数据
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public <T> Result<T> success(@Nullable T data) {
        return Result.success(data);
    }

    /**
     * failure
     *
     * @since 2020/3/31 10:59
     * @param errmsg 错误信息
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public <T> Result<T> failure(String errmsg) {
        return Result.failure(GlobalErrorCode.INTERNAL_SERVER_ERROR, errmsg);
    }

}
