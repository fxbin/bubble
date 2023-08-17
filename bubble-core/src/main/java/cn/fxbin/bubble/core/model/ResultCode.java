package cn.fxbin.bubble.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResultCode
 *
 * 0 标识成功， -1 为默认失败状态码
 *
 * 一般情况下，建议使用  <a href="https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status">HTTP 响应状态码</a>
 *
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 17:19
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 接口调用成功
     */
    SUCCESS(0, "Request Successful"),

    /**
     * 服务器暂不可用，建议稍候重试。建议重试次数不超过3次。
     */
    FAILURE(-1, "System Busy"),

    /**
     * 请求参数有误
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * Unauthorized
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * Forbidden
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * 找不到地址
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * 不支持当前请求方法
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    /**
     * 不接受的媒体类型
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

    /**
     * 服务异常
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),


    // === 自定义
    // 400|20--40  参数异常[20-30]  token认证问题异常[31-35]

    /**
     * 消息不能读取
     */
    MESSAGE_NOT_READABLE(40405,"Message Not Readable"),

    /**
     * 重定向至登录页面
     */
    REDIRECT_LOGIN_CODE(30302, "Please login again"),


    /**
     * 请求参数缺失
     */
    REQUEST_PARAM_MISSING_ERROR(40021, "Request parameter missing error"),

    /**
     * 请求参数格式有误
     */
    REQUEST_PARAM_FORMAT_ERROR(40023, "Request parameter format is incorrect"),

    /**
     * 请求参数校验错误
     */
    REQUEST_PARAM_VALIDATION_ERROR(40025, "Request parameter validation error"),

    /**
     * access toekn 过期
     */
    AUTHORIZATION_ACCESS_TOKEN_EXPIRED(40031, "access token 过期"),

    /**
     * refresh token 过期
     */
    AUTHORIZATION_REFRESH_TOKEN_EXPIRED(40032, "refresh token 过期");


    final int code;

    final String msg;
}
