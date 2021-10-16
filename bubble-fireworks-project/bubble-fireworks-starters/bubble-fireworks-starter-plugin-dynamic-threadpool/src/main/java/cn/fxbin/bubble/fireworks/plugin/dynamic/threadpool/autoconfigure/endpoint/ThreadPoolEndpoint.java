package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.autoconfigure.endpoint;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.autoconfigure.DynamicThreadPoolProperties;
import cn.fxbin.bubble.fireworks.core.util.MathUtils;
import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.model.ThreadPoolInfo;
import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.support.ThreadPoolExecutorOperations;
import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.support.ThreadPoolRejectedRecordOperations;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ThreadPoolEndpoint
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/7 16:38
 */
@Endpoint(
        id = "threadpool"
)
@ConditionalOnClass({ThreadPoolInfo.class})
public class ThreadPoolEndpoint {

    @Resource
    private DynamicThreadPoolProperties dynamicThreadPoolProperties;

    private final ThreadPoolExecutorOperations operations;
    private final ThreadPoolRejectedRecordOperations rejectedRecordOperations;

    public ThreadPoolEndpoint(ThreadPoolExecutorOperations operations, ThreadPoolRejectedRecordOperations rejectedRecordOperations) {
        this.operations = operations;
        this.rejectedRecordOperations = rejectedRecordOperations;
    }

    @ReadOperation
    public Map<String, Map<String, List<ThreadPoolInfo>>> threadPoolInfo() {

        String appName = dynamicThreadPoolProperties.getApplicationName();

        List<ThreadPoolInfo> list = new ArrayList<>();
        dynamicThreadPoolProperties.getPool().forEach(threadPoolProperty -> {
            String poolName = threadPoolProperty.getPoolName();
            ThreadPoolExecutor executor = operations.get(poolName);

            ThreadPoolInfo threadPoolInfo = ThreadPoolInfo.builder()
                    .poolName(poolName)
                    .corePoolSize(executor.getCorePoolSize())
                    .activeCount(executor.getActiveCount())
                    .queueType(executor.getQueue().getClass().getSimpleName())
                    .rejectedPolicy(threadPoolProperty.getRejectedPolicy())
                    .maximumPoolSize(executor.getMaximumPoolSize())
                    .activeRate(MathUtils.multiply(
                            MathUtils.divide(executor.getActiveCount(), executor.getMaximumPoolSize()),
                            BigDecimal.valueOf(100)))
                    .completedTaskCount(executor.getCompletedTaskCount())
                    .queueCapacity(threadPoolProperty.getCapacity())
                    .queueSize(executor.getQueue().size())
                    .queueRemainingCapacity(executor.getQueue().remainingCapacity())
                    .largestPoolSize(executor.getLargestPoolSize())
                    .rejectCount(rejectedRecordOperations.get(poolName))
                    .build();
            list.add(threadPoolInfo);
        });

        return build(appName, list);
    }


    private Map<String, Map<String, List<ThreadPoolInfo>>> build(String appName, List<ThreadPoolInfo> list) {
        Map<String, Map<String, List<ThreadPoolInfo>>> map = new HashMap<>();
        Map<String, List<ThreadPoolInfo>> threadInfoMap = new HashMap<>();
        threadInfoMap.put(appName, list);
        map.put("threadpool", threadInfoMap);
        return map;
    }


}
