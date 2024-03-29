package cn.fxbin.bubble.web.handler;

import cn.fxbin.bubble.core.dataobject.ErrorCode;
import cn.fxbin.bubble.core.dataobject.GlobalErrorCode;
import cn.fxbin.bubble.core.dataobject.Result;
import cn.fxbin.bubble.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

/**
 * DefaultGlobalExceptionHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/2 13:38
 */
@Slf4j
@RestControllerAdvice
public class DefaultGlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    public Result<String> exceptionHandler(Exception exception) {
        log.warn("[Exception]", exception);
        return Result.failure(exception.getMessage());
    }

    @ExceptionHandler(value = ServiceException.class)
    public Result<String> exceptionHandler(ServiceException ex) {
        log.warn("[ServiceException]", ex);
        return Result.failure(Objects.requireNonNull(ErrorCode.valueOf(ex.getErrcode())).isError() ? ex.getErrcode() : GlobalErrorCode.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    /**
     * 处理 SpringMVC 请求参数缺失
     *
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException exception) {
        log.warn("MissingServletRequestParameterExceptionHandler", exception);
        return Result.failure(GlobalErrorCode.BAD_REQUEST, String.format("请求参数缺失:%s", exception.getParameterName()));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException exception) {
        log.warn("MethodArgumentTypeMismatchExceptionHandler", exception);
        return Result.failure(GlobalErrorCode.BAD_REQUEST, String.format("请求参数类型错误:%s", exception.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<?> constraintViolationExceptionHandler(ConstraintViolationException exception) {
        log.warn("ConstraintViolationExceptionHandler", exception);
        ConstraintViolation<?> constraintViolation = exception.getConstraintViolations().iterator().next();
        return Result.failure(GlobalErrorCode.BAD_REQUEST, String.format("请求参数不正确:%s", constraintViolation.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result<?> bodyValidExceptionHandler(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        // 取出所有的校验不通过的描述
        List<String> fieldErrorMessages = fieldErrors.stream().map(FieldError::getDefaultMessage).toList();
        log.warn("MethodArgumentNotValidException: {}", exception.getMessage());
        return Result.failure(GlobalErrorCode.BAD_REQUEST, fieldErrorMessages.toString());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})
    public Result<?> bindExceptionHandler(BindException exception) {
        log.warn("BindException: {}", exception.getMessage());
        FieldError fieldError = exception.getFieldError();
        // 断言，避免告警
        assert fieldError != null;
        return Result.failure(GlobalErrorCode.BAD_REQUEST,
                ObjectUtils.isEmpty(fieldError.getDefaultMessage()) ? GlobalErrorCode.BAD_REQUEST.reasonPhrase(): fieldError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonParseException.class})
    public Result<?> exceptionHandler(JsonParseException exception) {
        log.warn("JsonParseException: {}", exception.getMessage());
        return Result.failure(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonMappingException.class})
    public Result<?> exceptionHandler(JsonMappingException exception) {
        log.warn("JsonMappingException: {}", exception.getMessage());
        return Result.failure(exception.getMessage());
    }

}
