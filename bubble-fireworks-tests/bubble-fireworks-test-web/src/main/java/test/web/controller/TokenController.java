package test.web.controller;

import cn.fxbin.bubble.plugin.token.DoubleJwt;
import cn.fxbin.bubble.plugin.token.model.Tokens;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * TokenController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/14 19:20
 */
@RestController
@RequestMapping("/token")
public class TokenController {


    @Resource
    private DoubleJwt doubleJwt;

    @GetMapping("/get")
    public Tokens get() {
        return doubleJwt.generateTokens("1");
    }

}
