package cn.fxbin.bubble.plugin.dynamic.threadpool.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * RejectedPolicy
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/6 15:42
 */
public enum RejectedPolicy {

    /**
     * 丢弃任务，并抛出 RejectedExecutionException
     */
    AbortPolicy("AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),

    /**
     * 丢弃任务，但是不抛出异常
     */
    DiscardPolicy("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),

    /**
     * 丢弃队列最前面的任务, 然后重新提交被拒绝的任务
     */
    DiscardOldestPolicy("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy()),

    /**
     * 由调用线程（提交任务的线程）处理该任务
     */
    CallerRunsPolicy("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy());

    RejectedPolicy(String name, RejectedExecutionHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    private String name;

    private RejectedExecutionHandler handler;

    public String getName() {
        return name;
    }

    public RejectedExecutionHandler getHandler() {
        return handler;
    }

    public static RejectedExecutionHandler match(String name, RejectedPolicy defaultRejectedPolicy) {
        Map<String, RejectedPolicy> map = Arrays.stream(RejectedPolicy.values()).collect(Collectors.toMap(
                RejectedPolicy::getName,
                rejectedPolicy -> rejectedPolicy
        ));

        ServiceLoader<RejectedExecutionHandler> serviceLoader = ServiceLoader.load(RejectedExecutionHandler.class);
        for (RejectedExecutionHandler rejectedExecutionHandler : serviceLoader) {
            String rejectedPolicyName = rejectedExecutionHandler.getClass().getSimpleName();
            if (name.equalsIgnoreCase(rejectedPolicyName)) {
                return rejectedExecutionHandler;
            }
        }

        return map.getOrDefault(name, defaultRejectedPolicy).getHandler();
    }
}
