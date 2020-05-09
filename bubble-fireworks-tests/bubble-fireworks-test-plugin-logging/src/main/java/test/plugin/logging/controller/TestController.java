package test.plugin.logging.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/8 18:22
 */
@Slf4j
@RestController
public class TestController {


    @GetMapping("/test")
    public String test(String name) {

        log.info("test:{} ", name);

        new Thread(() -> log.info("thread test:{}", name)).start();

        return "test";
    }

}
