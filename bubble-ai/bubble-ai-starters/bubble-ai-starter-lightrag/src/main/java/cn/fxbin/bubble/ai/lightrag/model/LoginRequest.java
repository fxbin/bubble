package cn.fxbin.bubble.ai.lightrag.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求模型
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/1/15 15:30
 */
@Data
public class LoginRequest implements Serializable {

    /**
     * 授权类型，固定为 "password"
     */
    private String grantType = "password";

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 授权范围
     */
    private String scope = "";

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

}