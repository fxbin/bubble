package cn.fxbin.bubble.plugin.token.model;

import cn.fxbin.bubble.fireworks.core.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tokens
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/11 16:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tokens {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

}
