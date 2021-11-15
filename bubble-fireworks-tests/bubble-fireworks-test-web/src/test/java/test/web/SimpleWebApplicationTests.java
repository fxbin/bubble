package test.web;

import cn.fxbin.bubble.plugin.token.DoubleJwt;
import cn.fxbin.bubble.plugin.token.model.TokenPayload;
import cn.fxbin.bubble.plugin.token.model.Tokens;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleWebApplicationTests
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/21 8:52
 */
@Slf4j
@SpringBootTest
public class SimpleWebApplicationTests {

    @Resource
    private DoubleJwt doubleJwt;

    @Test
    public void context() {
        Tokens tokens = doubleJwt.generateTokens(1);
        log.info("tokens: {}", tokens);
        assertNotNull(tokens);
    }

    @Test
    public void parseToken() {
        TokenPayload payload = doubleJwt.parseToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpZGVudGl0eSI6IjEiLCJzY29wZSI6ImJ1YmJsZS1maXJld29ya3MiLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNjA1OTIxNDIyLCJleHAiOjE2MDU5MjE1MjJ9.C5IMP_knsMZA-ojufGvWDZxBOWzfPGILQiOPpVmObahRdvPdCLk2q8Q7W3wJPZFTK46sd9e9nFgTG2dwh3pumg");
        log.info("payload: {}", payload);
        assertNotNull(payload);
    }

}
