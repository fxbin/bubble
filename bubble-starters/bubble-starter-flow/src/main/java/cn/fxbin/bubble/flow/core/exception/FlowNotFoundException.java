package cn.fxbin.bubble.flow.core.exception;


import cn.fxbin.bubble.core.exception.ServiceException;

/**
 * FlowNotFoundException
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:34
 */
public class FlowNotFoundException extends ServiceException {

    public FlowNotFoundException(String flowId) {
        super("流程不存在: " + flowId);
    }

}
