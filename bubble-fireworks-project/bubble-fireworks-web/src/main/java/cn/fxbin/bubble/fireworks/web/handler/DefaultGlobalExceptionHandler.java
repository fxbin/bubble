package cn.fxbin.bubble.fireworks.web.handler;

import cn.fxbin.bubble.fireworks.core.exception.ServiceException;
import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.core.model.ResultCode;
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
    public Result exceptionHandler(Exception exception) {
        log.warn("[Exception]", exception);
        return Result.failure(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = ServiceException.class)
    public Result exceptionHandler(ServiceException ex) {
        log.warn("[ServiceException]", ex);
        return Result.failure((ex.getErrcode() == 0 ? ResultCode.FAILURE.getCode() : ex.getErrcode()), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result bodyValidExceptionHandler(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        log.warn(fieldErrors.get(0).getDefaultMessage());
        return Result.failure(fieldErrors.get(0).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})
    public Result bindExceptionHandler(BindException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        log.warn(fieldErrors.get(0).getDefaultMessage());
        return Result.failure(fieldErrors.get(0).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonParseException.class})
    public Result exceptionHandler(JsonParseException exception) {
        log.error("JsonParseException: {}", exception.getMessage());
        return Result.failure(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonMappingException.class})
    public Result exceptionHandler(JsonMappingException exception) {
        log.error("JsonMappingException: {}", exception.getMessage());
        return Result.failure(exception.getMessage());
    }

}
