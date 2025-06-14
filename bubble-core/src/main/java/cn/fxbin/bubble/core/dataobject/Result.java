package cn.fxbin.bubble.core.dataobject;

import cn.fxbin.bubble.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "响应信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 5392304127156442143L;

    @Schema(description = "状态返回码", requiredMode = Schema.RequiredMode.REQUIRED)
    private int errcode;

    @Schema(description = "对返回码的文本描述内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String errmsg;

    @Schema(description = "响应数据")
    private T data;

    public Result() {
    }

    private Result(ErrorCode errorCode) {
        this(errorCode.value(), errorCode.reasonPhrase());
    }

    private Result(ErrorCode errorCode, T data) {
        this(errorCode.value(), errorCode.reasonPhrase(), data);
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
                .map(code -> GlobalErrorCode.OK.value() == code)
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
        return Optional.ofNullable(result).isPresent() ? Optional.of(result.errcode).get() : GlobalErrorCode.INTERNAL_SERVER_ERROR.value();
    }


    /**
     * getErrMsg
     *
     * @since 2020/3/25 22:47
     * @param result cn.fxbin.bubble.core.model.Result
     * @return java.lang.String
     */
    public static String getErrMsg(@Nullable Result<?> result) {
        return Optional.ofNullable(result).isPresent() ? Optional.of(result.errmsg).get() : GlobalErrorCode.INTERNAL_SERVER_ERROR.reasonPhrase();
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
                .filter(r -> r.errcode == GlobalErrorCode.OK.value())
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
        return new Result<>(GlobalErrorCode.OK);
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
        return new Result<>(GlobalErrorCode.OK, data);
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
     * @param errorCode cn.fxbin.bubble.core.model.ResultesultCode
     * @param <T> 泛型标记
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> status(boolean status, ErrorCode errorCode) {
        return status ? Result.success() : Result.failure(errorCode);
    }


    /**
     * failure
     *
     * @since 2020/3/25 22:52
     * @param errmsg 错误信息
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> failure(String errmsg) {
        return new Result<>(GlobalErrorCode.INTERNAL_SERVER_ERROR.value(), errmsg);
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
     * 失败
     * failure
     *
     * @param errmsg    错误信息
     * @param errorCode 错误代码
     * @return cn.fxbin.bubble.core.model.Result<T>
     * @since 2020/3/25 22:52
     */
    public static <T> Result<T> failure(ErrorCode errorCode, String errmsg) {
        return new Result<>(errorCode.value(), errmsg);
    }


    /**
     * failure
     *
     * @since 2020/3/25 22:53
     * @param errorCode cn.fxbin.bubble.core.model.ResultesultCode
     * @return cn.fxbin.bubble.core.model.Result<T>
     */
    public static <T> Result<T> failure(ErrorCode errorCode) {
        return new Result<>(errorCode);
    }

    /**
     * failure
     *
     * @param exception 例外
     * @return {@link Result<T>}
     */
    public static <T> Result<T> failure(ServiceException exception) {
        return new Result<>(exception.getErrcode(), exception.getErrmsg());
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
