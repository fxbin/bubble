package cn.fxbin.bubble.plugin.token.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * TokenClaims
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/13 16:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPayload {

    /**
     * 标识token type
     */
    private String type;

    /**
     * 身份标识，ex: userId
     */
    private String identity;

    /**
     * 请求标识码 ex: web, app
     */
    private String scope;

    /**
     * 附加信息，用户自定义信息
     */
    private Map<String, Object> extra;

    /**
     * 当前时间戳
     */
    private Integer iat;

    /**
     * 过期时间戳
     */
    private Integer exp;

}
