package cn.fxbin.bubble.flow.core.exception;

import cn.fxbin.bubble.core.exception.ServiceException;

/**
 * CycleDetectedException
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 10:12
 */
public class CycleDetectedException extends ServiceException {

    public CycleDetectedException(String msg) {
        super(msg);
    }

}
