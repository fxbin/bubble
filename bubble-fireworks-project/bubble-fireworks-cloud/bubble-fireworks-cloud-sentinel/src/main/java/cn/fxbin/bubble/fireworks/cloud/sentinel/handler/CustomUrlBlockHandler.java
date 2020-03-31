package cn.fxbin.bubble.fireworks.cloud.sentinel.handler;

import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.core.util.JsonUtils;
import cn.fxbin.bubble.fireworks.core.util.MimeTypeUtils;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CustomUrlBlockHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/31 16:52
 */
@Slf4j
public class CustomUrlBlockHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        log.error("sentinel 降级 资源名称{}", e.getRule().getResource(), e);

        response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().print(
                JsonUtils.toJson(Result.failure("Blocked by Sentinel (flow limiting)")));
    }
}
