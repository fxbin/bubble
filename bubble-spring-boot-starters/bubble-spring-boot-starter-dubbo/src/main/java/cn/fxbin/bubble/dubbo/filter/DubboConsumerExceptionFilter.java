package cn.fxbin.bubble.dubbo.filter;

import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.dubbo.constant.DubboConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;

/**
 * DubboConsumerExceptionFilter
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/11/8 11:27
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER})
public class DubboConsumerExceptionFilter implements Filter, Filter.Listener {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        Map<String, String> attach = appResponse.getAttachments();
        String code = attach.get(DubboConst.CODE);
        String msg = attach.get(DubboConst.MSG);
        if (StringUtils.isNotEmpty(code)) {
            appResponse.setException(new ServiceException(Integer.parseInt(code), msg));
        }
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        log.error("Got unchecked and undeclared exception which called by " + RpcContext.getServiceContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName() + ", exception: " + t.getClass().getName() + ": " + t.getMessage(), t);
    }
}
