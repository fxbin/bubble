package cn.fxbin.bubble.plugin.dynamic.threadpool.wrapper;

import cn.fxbin.bubble.plugin.dynamic.threadpool.support.ThreadPoolRejectedRecordOperations;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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

        String msg = String.format("Thread pool is EXHAUSTED!" +
                        " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                        " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)",
                poolName, executor.getPoolSize(), executor.getActiveCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getLargestPoolSize(),
                executor.getTaskCount(), executor.getCompletedTaskCount(), executor.isShutdown(), executor.isTerminated(), executor.isTerminating());
        log.warn(msg);

        rejectedRecordOperations.put(poolName);
    }
}
