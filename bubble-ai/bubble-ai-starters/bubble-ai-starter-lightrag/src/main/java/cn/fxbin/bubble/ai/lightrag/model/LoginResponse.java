package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 登录响应模型
 *
 * <p>示例数据：
 * <pre>
 * {
 *   "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJndWVzdCIsImV4cCI6MTc1NjM3MDc4OSwicm9sZSI6Imd1ZXN0IiwibWV0YWRhdGEiOnsiYXV0aF9tb2RlIjoiZGlzYWJsZWQifX0._hB92zYLp_3Vbk7r0q-T1So-nmj1kXkaFQWp1KMG9sM",
 *   "token_type": "bearer",
 *   "auth_mode": "disabled",
 *   "message": "Authentication is disabled. Using guest access.",
 *   "core_version": "1.4.6",
 *   "api_version": "0198",
 *   "webui_title": null,
 *   "webui_description": null
 * }
 * </pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/1/15 15:30
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class LoginResponse extends BaseResponse implements Serializable {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("auth_mode")
    private String authMode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("core_version")
    private String coreVersion;

    @JsonProperty("api_version")
    private String apiVersion;

    @JsonProperty("webui_title")
    private String webuiTitle;

    @JsonProperty("webui_description")
    private String webuiDescription;
}