package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.wrapper;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.support.ThreadPoolRejectedRecordOperations;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedExecutionHandlerWrapper
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/10 15:38
 */
public class RejectedExecutionHandlerWrapper implements RejectedExecutionHandler {

    private final String poolName;

    private final ThreadPoolRejectedRecordOperations rejectedRecordOperations;

    private final RejectedExecutionHandler rejectedExecutionHandler;


    public RejectedExecutionHandlerWrapper(String poolName, ThreadPoolRejectedRecordOperations rejectedRecordOperations, RejectedExecutionHandler rejectedExecutionHandler) {
        this.poolName = poolName;
        this.rejectedRecordOperations = rejectedRecordOperations;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    /**
     * Method that may be invoked by a {@link ThreadPoolExecutor} when
     * {@link ThreadPoolExecutor#execute execute} cannot accept a
     * task.  This may occur when no more threads or queue slots are
     * available because their bounds would be exceeded, or upon
     * shutdown of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link RejectedExecutionException}, which will be
     * propagated to the caller of {@code execute}.
     *
     * @param r        the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     * @throws RejectedExecutionException if there is no remedy
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        rejectedExecutionHandler.rejectedExecution(r, executor);
        rejectedRecordOperations.put(poolName);
    }
}
