package test.plugin.swagger.controller;

import cn.fxbin.bubble.fireworks.core.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/11 17:50
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public Result test() {
        return Result.success();
    }

}
