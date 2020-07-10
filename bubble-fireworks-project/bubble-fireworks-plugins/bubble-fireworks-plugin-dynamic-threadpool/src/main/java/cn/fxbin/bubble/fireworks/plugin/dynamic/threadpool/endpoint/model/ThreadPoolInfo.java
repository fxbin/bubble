package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.endpoint.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ThreadPoolInfo
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/10 10:36
 */
@Data
@Builder
public class ThreadPoolInfo implements Serializable {

    private static final long serialVersionUID = -7485244300668095977L;

    /**
     * 线程池名称
     */
    private String poolName;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 活动线程数
     */
    private Integer activeCount;

    /**
     * 队列类型
     */
    private String queueType;

    /**
     * 拒绝策略
     */
    private String rejectedPolicy;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 线程池活跃度 = activeCount/maximumPoolSize
     */
    private BigDecimal activeRate;

    /**
     * 任务完成数
     */
    private Long completedTaskCount;

    /**
     * 队列大小
     */
    private Integer queueCapacity;

    /**
     * 队列剩余大小
     */
    private Integer queueRemainingCapacity;

    /**
     * 排队线程数
     */
    private Integer queueSize;


    /**
     * 最大线程数
     */
    private Integer largestPoolSize;

    /**
     * 拒绝数量
     */
    private Long rejectCount;

}
