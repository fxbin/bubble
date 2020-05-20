package test.plugin.logging.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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


    @GetMapping("/test/{name}")
    public String test(@PathVariable("name") String name) {

        log.info("test:{} ", name);

        new Thread(() -> log.info("thread test:{}", name)).start();

        return "test";
    }

    @PostMapping("/test")
    public String test(@RequestBody Map<String, Object> map) {

        log.info("test:{} ", map);

        new Thread(() -> log.info("thread test:{}", map)).start();

        return "test post";
    }

}
