package cn.fxbin.bubble.fireworks.web.handler;

import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.core.model.ResultCode;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DefaultController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/27 18:58
 */
@RestController
public class DefaultController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public Result<?> error() {
        return Result.failure(ResultCode.NOT_FOUND);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

}
