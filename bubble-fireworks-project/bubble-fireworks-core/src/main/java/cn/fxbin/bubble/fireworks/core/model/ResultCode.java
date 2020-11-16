package cn.fxbin.bubble.fireworks.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResultCode
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
     * 找不到地址
     */
    NOT_FOUND(40404, "Not Found"),

    /**
     * 消息不能读取
     */
    MESSAGE_NOT_READABLE(40405,"Message Not Readable"),

    /**
     * 重定向至登录页面
     */
    REDIRECT_LOGIN_CODE(30302, "Please login again"),

    /**
     * 不接受的媒体类型
     */
    UNSUPPORTED_MEDIA_TYPE(41415, "Unsupported Media Type"),

    /**
     * 不支持当前请求方法
     */
    METHOD_NOT_ALLOWED(40405, "Method Not Allowed"),

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


    // 400|20--40  参数异常[20-30]  token认证问题异常[31-35]



    final int code;

    final String msg;
}
