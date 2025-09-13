package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 认证状态响应模型
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/1/15 15:30
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class AuthStatusResponse extends LoginResponse implements Serializable{

    // {
    //  "auth_configured": false,
    //  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJndWVzdCIsImV4cCI6MTc1NjM2OTUzMSwicm9sZSI6Imd1ZXN0IiwibWV0YWRhdGEiOnsiYXV0aF9tb2RlIjoiZGlzYWJsZWQifX0.KpnnonciFAvfIntVCvUEIwH3xdirNWzYGkbJqK9z-uE",
    //  "token_type": "bearer",
    //  "auth_mode": "disabled",
    //  "message": "Authentication is disabled. Using guest access.",
    //  "core_version": "1.4.6",
    //  "api_version": "0198",
    //  "webui_title": null,
    //  "webui_description": null
    //}

    @JsonProperty("auth_configured")
    private boolean authConfigured;


}