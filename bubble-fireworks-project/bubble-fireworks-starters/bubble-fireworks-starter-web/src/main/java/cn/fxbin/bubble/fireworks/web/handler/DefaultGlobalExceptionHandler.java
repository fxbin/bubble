package cn.fxbin.bubble.fireworks.web.handler;

import cn.fxbin.bubble.fireworks.core.exception.ServiceException;
import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.core.model.ResultCode;
import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static cn.fxbin.bubble.fireworks.core.model.ResultCode.REQUEST_PARAM_VALIDATION_ERROR;

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

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public Result<String> exceptionHandler(Exception exception) {
        log.warn("[Exception]", exception);
        return Result.failure(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = ServiceException.class)
    public Result<String> exceptionHandler(ServiceException ex) {
        log.warn("[ServiceException]", ex);
        return Result.failure((ex.getErrcode() == 0 ? ResultCode.FAILURE.getCode() : ex.getErrcode()), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result<String> bodyValidExceptionHandler(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        log.warn("MethodArgumentNotValidException: {}", exception.getMessage());
        return Result.failure(fieldErrors.get(0).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})
    public Result<String> bindExceptionHandler(BindException exception) {
        @SuppressWarnings("ConstantConditions") String defaultMessage = exception.getGlobalError().getDefaultMessage();
        log.warn("BindException: {}", exception.getMessage());
        return Result.failure(ObjectUtils.isEmpty(defaultMessage) ? REQUEST_PARAM_VALIDATION_ERROR.getMsg() : defaultMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonParseException.class})
    public Result<String> exceptionHandler(JsonParseException exception) {
        log.warn("JsonParseException: {}", exception.getMessage());
        return Result.failure(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonMappingException.class})
    public Result<String> exceptionHandler(JsonMappingException exception) {
        log.warn("JsonMappingException: {}", exception.getMessage());
        return Result.failure(exception.getMessage());
    }

}
