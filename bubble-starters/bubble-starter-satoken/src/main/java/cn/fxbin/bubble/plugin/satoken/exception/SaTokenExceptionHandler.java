package cn.fxbin.bubble.plugin.satoken.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.fxbin.bubble.core.dataobject.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SaTokenExceptionHandler
 * Sa-Token 全局异常处理
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 15:40
 */
@Slf4j
@Order(100)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 处理 NotLoginException 异常
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        String message;
        if (e.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供Token";
        } else if (e.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "Token无效";
        } else if (e.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "Token已过期";
        } else if (e.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "Token已被顶下线";
        } else if (e.getType().equals(NotLoginException.KICK_OUT)) {
            message = "Token已被踢下线";
        } else {
            message = "当前会话未登录";
        }
        return Result.builder().errcode(HttpStatus.UNAUTHORIZED.value()).errmsg(message).build();
    }

    /**
     * 处理 NotRoleException 异常
     * 角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleNotRoleException(NotRoleException e) {
        log.warn("角色权限不足: {}", e.getMessage());
        return Result.builder().errcode(HttpStatus.FORBIDDEN.value()).errmsg("角色权限不足: " + e.getMessage()).build();
    }

    /**
     * 处理 NotPermissionException 异常
     * 权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.builder().errcode(HttpStatus.FORBIDDEN.value()).errmsg("权限不足: " + e.getMessage()).build();
    }

    /**
     * 处理 SaTokenException 异常
     * Sa-Token 异常
     */
    @ExceptionHandler(SaTokenException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleSaTokenException(SaTokenException e) {
        log.error("Sa-Token 异常", e);
        return Result.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).errmsg("Sa-Token 异常: " + e.getMessage()).build();
    }


    /**
     * 统一处理 Sa-Token 异常
     */
    public static Result<Object> handleEx(Exception e) {
        if (e instanceof NotLoginException) {
            return new SaTokenExceptionHandler().handleNotLoginException((NotLoginException) e);
        } else if (e instanceof NotRoleException) {
            return new SaTokenExceptionHandler().handleNotRoleException((NotRoleException) e);
        } else if (e instanceof NotPermissionException) {
            return new SaTokenExceptionHandler().handleNotPermissionException((NotPermissionException) e);
        } else {
            return new SaTokenExceptionHandler().handleSaTokenException((SaTokenException) e);
        }
    }

}