package cn.fxbin.bubble.plugin.token;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias("access_token")
    private String accessToken;

    @JsonAlias("refresh_token")
    private String refreshToken;

}
