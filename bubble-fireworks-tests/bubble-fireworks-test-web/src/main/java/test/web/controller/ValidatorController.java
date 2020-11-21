package test.web.controller;

import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.web.validator.EqualField;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import test.web.dto.TestEqualDTO;
import test.web.dto.TestMessageDTO;

import javax.validation.constraints.NotBlank;

/**
 * ValidatorController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/16 0:55
 */
@Validated
@RestController
@RequestMapping("/validator")
public class ValidatorController {

    @GetMapping("/equal")
    public Result equal(@Validated TestEqualDTO testEqualDTO) {
        return Result.success(testEqualDTO);
    }

    /**
     * un support this method
     */
    @GetMapping("/equal_param")
    public Result equal(@EqualField(source = "a", target = "b") String a, String b) {
        return Result.success(a + ":" + b);
    }

    @GetMapping("/test")
    public Result<?> testMessage(@NotBlank(message = "{test.aaa}") String test) {
        return Result.success(test);
    }

    @PostMapping("/testJsonMessage")
    public Result<?> testJsonMessage(@RequestBody @Validated TestMessageDTO test) {
        return Result.success(test);
    }

    @PostMapping("/testFormMessage")
    public Result<?> testFormMessage(@Validated TestMessageDTO test) {
        return Result.success(test);
    }

}
