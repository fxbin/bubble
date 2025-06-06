package cn.fxbin.bubble.plugin.satoken.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Tokens
 * Token 模型类
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 15:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tokens implements Serializable {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

}