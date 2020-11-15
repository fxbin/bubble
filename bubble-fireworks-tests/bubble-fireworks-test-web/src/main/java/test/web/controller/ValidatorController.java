package test.web.controller;

import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.web.validator.EqualField;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.web.dto.TestEqualDTO;

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

}
