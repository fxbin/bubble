package cn.fxbin.bubble.flow.core.exception;


import cn.fxbin.bubble.core.exception.ServiceException;

/**
 * FlowNotPublishedException
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:35
 */
public class FlowNotPublishedException extends ServiceException {

    public FlowNotPublishedException(String flowId) {
        super("流程未发布: " + flowId);
    }

}
