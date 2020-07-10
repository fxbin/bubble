package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.customizer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RetryAndLogPolicy
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/7 15:31
 */
public class RetryAndLogPolicy extends ThreadPoolExecutor.CallerRunsPolicy {

    private final Logger logger = LoggerFactory.getLogger(RetryAndLogPolicy.class);

    /**
     * Executes task r in the caller's thread, unless the executor
     * has been shut down, in which case the task is discarded.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        super.rejectedExecution(r, e);
        ThreadFactory factory = e.getThreadFactory();
        String threadNamePrefix = "";
        if (factory instanceof CustomizableThreadFactory) {
            CustomizableThreadFactory ctf = (CustomizableThreadFactory) factory;
            threadNamePrefix = ctf.getThreadNamePrefix();
        }

        logger.warn("threadNamePrefix {}, corePoolSize {}, maxPoolSize {}, workQueueSize {}, rejected task {}",
                threadNamePrefix, e.getCorePoolSize(), e.getMaximumPoolSize(), e.getQueue().size(), r.toString());
    }
}
