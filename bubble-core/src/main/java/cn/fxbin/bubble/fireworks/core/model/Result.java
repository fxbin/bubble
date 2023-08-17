package cn.fxbin.bubble.fireworks.core.model;

import cn.fxbin.bubble.fireworks.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Optional;

/**
 * Result
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 16:59
 */
@Data
@Builder
@Accessors(chain = true)
@ApiModel(description = "响应信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 5392304127156442143L;

    @ApiModelProperty(value = "状态返回码", required = true)
    private int errcode;

    @ApiModelProperty(value = "对返回码的文本描述内容", required = true)
    private String errmsg;

    @ApiModelProperty(value = "响应数据")
    private T data;

    public Result() {
    }

    private Result(ResultCode resultCode) {
        this(resultCode.code, resultCode.msg);
    }

    private Result(ResultCode resultCode, T data) {
        this(resultCode.code, resultCode.msg, data);
    }

    private Result(int errcode, String errmsg) {
        this(errcode, errmsg, null);
    }

    public Result(int errcode, String errmsg, T data) {
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.data = data;
    }


    /**
     * isSuccess 判断返回是否为成功
     *
     * @since 2020/3/25 22:46
     * @param result cn.fxbin.bubble.core.model.Result
     * @return boolean
     */
    public static boolean isSuccess(@Nullable Result<?> result) {
        return Optional.ofNullable(result)
                .map(r -> r.errcode)
                .map(code -> ResultCode.SUCCESS.code == code)
                .orElse(Boolean.FALSE);
    }


    /**
     * isNotSuccess
     *
     * @since 2020/3/25 22:46
     * @param result cn.fxbin.bubble.core.model.Result
     * @return boolean
     */
    public static boolean isNotSuccess(@Nullable Result<?> result) {
        return !Result.isSuccess(result);
    }


    /**
     * getErrCode
     *
     * @since 2020/3/25 22:47
     * @param result cn.fxbin.bubble.core.model.Result
     * @return java.lang.Integer
     */
    public static Integer getErrCode(@Nullable Result<?> result) {
        return Optional.ofNullable(result).isPresent() ? Optional.of(result.errcode).get() : ResultCode.FAILURE.code;
    }


    /**
     * getErrMsg
     *
     * @since 2020/3/25 22:47
     * @param result cn.fxbin.bubble.core.model.Result
     * @return java.lang.String
     */
    public static String getErrMsg(@Nullable Result<?> result) {
        return Optional.ofNullable(result).isPresent() ? Optional.of(result.errmsg).get() : ResultCode.FAILURE.msg;
    }


    /**
     * getData
     *
     * @since 2020/3/25 22:48
     * @param result cn.fxbin.bubble.core.model.Result
     * @param <T> 泛型标记
     * @return T
     */
    @Nullable
    public static <T> T getData(@Nullable Result<T> result) {
        return Optional.ofNullable(result)
                .filter(r -> r.errcode == ResultCode.SUCCESS.code)
                .map(r -> r.data)
                .orElse(null);
    }


    /**
     * success
     *
     * @since 2020/3/25 22:49
     * @param <T> 泛型标记
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS);
    }


    /**
     * success
     *
     * @since 2020/3/25 22:49
     * @param data cn/fxbin/bubble/core/model/R.java:140
     * @param <T> 泛型标记
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> success(@Nullable T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }


    /**
     * status
     *
     * @since 2020/3/25 22:50
     * @param status 错误状态
     * @param errmsg 错误信息
     * @param <T> 泛型标记
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> status(boolean status, String errmsg) {
        return status ? Result.success() : Result.failure(errmsg);
    }


    /**
     * status
     *
     * @since 2020/3/25 22:51
     * @param status 错误状态
     * @param resultCode cn.fxbin.bubble.core.model.ResultesultCode
     * @param <T> 泛型标记
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> status(boolean status, ResultCode resultCode) {
        return status ? Result.success() : Result.failure(resultCode);
    }


    /**
     * failure
     *
     * @since 2020/3/25 22:52
     * @param errmsg 错误信息
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> failure(String errmsg) {
        return new Result<>(ResultCode.FAILURE.code, errmsg);
    }


    /**
     * failure
     *
     * @since 2020/3/25 22:52
     * @param errcode 系统错误响应码
     * @param errmsg 错误信息
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> failure(int errcode, String errmsg) {
        return new Result<>(errcode, errmsg);
    }


    /**
     * failure
     *
     * @since 2020/3/25 22:52
     * @param resultCode cn.fxbin.bubble.core.model.ResultesultCode
     * @param errmsg 错误信息
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> failure(ResultCode resultCode, String errmsg) {
        return new Result<>(resultCode.code, errmsg);
    }


    /**
     * failure
     *
     * @since 2020/3/25 22:53
     * @param resultCode cn.fxbin.bubble.core.model.ResultesultCode
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return new Result<>(resultCode);
    }


    /**
     * throwOnFail 当 result 不成功时：直接抛出失败异常，返回传入的 result。
     *
     * @since 2020/3/25 22:53
     * @param result cn.fxbin.bubble.core.model.Result
     */
    public static void throwOnFail(Result<?> result) {
        if (Result.isNotSuccess(result)) {
            throw new ServiceException(Result.getErrMsg(result));
        }
    }


   /**
    * throwOnFail
    *
    * @since 2020/3/25 22:54
    * @param errmsg error message
    */
    public static void throwOnFail(String errmsg) {
        throw new ServiceException(errmsg);
    }

}
