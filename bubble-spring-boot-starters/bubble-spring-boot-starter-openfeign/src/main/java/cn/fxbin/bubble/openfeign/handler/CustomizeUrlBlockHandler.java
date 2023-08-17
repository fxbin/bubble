package cn.fxbin.bubble.openfeign.handler;

import cn.fxbin.bubble.core.model.Result;
import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.core.util.MimeTypeUtils;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
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
public class CustomizeUrlBlockHandler implements BlockExceptionHandler {

    /**
     * handle
     *
     * @since 2020/11/1 23:40
     * @param request http servlet request
     * @param response http servlet response
     * @param e com.alibaba.csp.sentinel.slots.block.BlockException
     *          <p>
     *              BlockException 包含以下几种异常
     *              FlowException    限流异常
     *              DegradeException    降级异常
     *              ParamFlowException    参数限流异常
     *              SystemBlockException    系统负载异常
     *              AuthorityException    授权异常
     *          </p>
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        log.error("sentinel 降级 资源名称{}", e.getRule().getResource(), e);

        Result<String> result = Result.failure("Blocked by Sentinel (flow limiting)");
        if (e instanceof FlowException){
            result.setErrmsg("Blocked by Sentinel (flow limiting)");
        }else if (e instanceof DegradeException){
            result.setErrmsg("Blocked by Sentinel (degrade limiting)");
        }else if (e instanceof ParamFlowException){
            result.setErrmsg("Blocked by Sentinel (param limiting)");
        }else if (e instanceof SystemBlockException){
            result.setErrmsg("Blocked by Sentinel (system limiting)");
        }else if (e instanceof AuthorityException){
            result.setErrmsg("Blocked by Sentinel (authority limiting)");
        }

        response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().print(
                JsonUtils.toJson(result));
    }
}
