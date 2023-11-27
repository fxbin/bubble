package cn.fxbin.bubble.dubbo.filter;

import cn.fxbin.bubble.core.dataobject.GlobalErrorCode;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.util.ExceptionUtils;
import cn.fxbin.bubble.dubbo.constant.DubboConst;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * DubboProviderExceptionFilter
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/11/9 15:09
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboProviderExceptionFilter implements Filter, BaseFilter.Listener {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException() && GenericService.class != invoker.getInterface()) {
            try {
                // 1. 转换异常
                Throwable exception = appResponse.getException();
                // 1.1 参数校验异常
                if (exception instanceof ConstraintViolationException) {
                    exception = this.constraintViolationExceptionHandler((ConstraintViolationException) exception);
                    // 1. ServiceException 业务异常，因为不会有序列化问题，所以无需处理
                } else {
                    exception = this.defaultExceptionHandler(exception, invocation);
                    Map<String, String> attachments = appResponse.getAttachments();
                    attachments.put(DubboConst.CODE, String.valueOf(((ServiceException) exception).getErrcode()));
                    attachments.put(DubboConst.MSG, ((ServiceException) exception).getErrmsg());
                }

                // 2. 根据不同的方法 schema 返回结果
                // 2.1 如果是 ServiceException 异常，并且返回参数类型是 Result 的情况，则将转换成 Result 返回
                if (isReturnCommonResult(invocation) && exception instanceof ServiceException) {
                    // 一定要清空异常
                    appResponse.setException(null);
                    appResponse.setValue(cn.fxbin.bubble.core.dataobject.Result.failure((ServiceException) exception));
                    // 2.2 如果是 GlobalException 全局异常，则直接抛出
                } else {
                    appResponse.setException(exception);
                    appResponse.getAttachments();
                }
            } catch (Throwable e) {
                log.warn("Fail to ExceptionFilter when called by " + RpcContext.getServiceContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName() + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        log.error("Got unchecked and undeclared exception which called by " + RpcContext.getServiceContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName() + ", exception: " + t.getClass().getName() + ": " + t.getMessage(), t);
    }

    private boolean isReturnCommonResult(Invocation invocation) {
        if (!(invocation instanceof RpcInvocation)) {
            return false;
        }
        RpcInvocation rpcInvocation = (RpcInvocation) invocation;
        Type[] returnTypes = rpcInvocation.getReturnTypes();
        if (returnTypes.length == 0) {
            return false;
        }
        Type returnType = returnTypes[0];
        if (!(returnType instanceof Class)) {
            return false;
        }
        Class returnClass = (Class) returnType;
        return returnClass == Result.class;
    }

    /**
     * 处理 Validator 校验不通过产生的异常
     */
    private ServiceException constraintViolationExceptionHandler(ConstraintViolationException ex) {
        log.warn("[constraintViolationExceptionHandler]", ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        return new ServiceException(GlobalErrorCode.BAD_REQUEST.value(),
                String.format("请求参数不正确:%s", constraintViolation.getMessage()));
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    private ServiceException defaultExceptionHandler(Throwable exception, Invocation invocation) {
        log.error("[defaultExceptionHandler][service({}) method({}) params({}) 执行异常] ===> {}",
                invocation.getTargetServiceUniqueName(), invocation.getMethodName(), invocation.getArguments(), ExceptionUtils.getStackTrace(exception));
        // 如果已经是 ServiceException 全局异常，直接返回即可
        if (exception instanceof ServiceException) {
            return (ServiceException) exception;
        }
        return new ServiceException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String buildDetailMessage(Throwable exception, Invocation invocation) {
        return String.format("Service(%s) Method(%s) 发生异常(%s)",
                invocation.getTargetServiceUniqueName(), invocation.getMethodName(), ExceptionUtils.getStackTrace(exception));
    }

}
