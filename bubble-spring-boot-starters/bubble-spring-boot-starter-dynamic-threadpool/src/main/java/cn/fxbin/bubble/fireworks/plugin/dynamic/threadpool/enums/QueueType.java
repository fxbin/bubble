package cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.enums;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.customizer.ResizableCapacityLinkedBlockIngQueue;

import java.util.concurrent.*;

/**
 * QueueType
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/6 15:31
 */
public enum QueueType {

    /**
     * 一个用数组实现的有界阻塞队列，此队列按照先进先出（FIFO) 的原则对元素进行排序，支持公平锁和非公平锁
     */
    ArrayBlockingQueue,

    /**
     * 一个链表结构组成的有界队列，此队列按照先进先出 (FIFO) 的原则对元素进行排序。默认长度 Integer.MAX_VALUE
     */
    LinkedBlockingQueue,

    /**
     * 一个支持线程优先级排序的无序队列，默认自然序进行排序，也可以自定义实现 compareTo() 方法来指定元素排序规则，不能保证同优先级元素的顺序
     */
    PriorityBlockingQueue,

    /**
     * 一个实现 PriorityBlockingQueue 实现延迟获取的无界队列，在创建元素时，可以指定多久才能从队列中获取当前元素。只有延时期满后才能从队列中获取元素
     */
    DelayQueue,

    /**
     * 一个不存储元素的阻塞队列，每一个put操作必须等到take操作，否则不能添加元素。支持公平锁和非公平锁。 SynchronousQueue 的一个使用场景是在线程池里。Executors.newCacheThreadPool()
     * 这个线程池根据需要 （新任务到来时） 创建新的线程，如果有空闲线程会重复使用，线程空闲了 60s 后会被回收
     */
    SynchronousQueue,

    /**
     * 一个由链表结构组成的无界阻塞队列，相当于其他队列，LinkedTransferQueue 队列多了 transfer 和 tryTransfer 方法
     */
    LinkedTransferQueue,

    /**
     * 一个由链表结构 组成的双向阻塞队列。队列头部和尾部都可以添加和移除元素，多线程并发是，可以将锁的竞争最多降到一半
     */
    LinkedBlockingDeque,

    /**
     * copy {@see java.util.concurrent.LinkedBlockingQueue}, remove capacity final keyword
     */
    ResizableCapacityLinkedBlockIngQueue;


    @SuppressWarnings("rawtypes")
    public static BlockingQueue getQueue(QueueType queueType, int capacity, boolean fair) {

        switch (queueType) {
            case ArrayBlockingQueue:
                return new ArrayBlockingQueue(capacity);
            case LinkedBlockingQueue:
                return new LinkedBlockingQueue(capacity);
            case PriorityBlockingQueue:
                return new PriorityBlockingQueue(capacity);
            case DelayQueue:
                return new DelayQueue();
            case SynchronousQueue:
                return new SynchronousQueue(fair);
            case LinkedTransferQueue:
                return new LinkedTransferQueue();
            case LinkedBlockingDeque:
                return new LinkedBlockingDeque(capacity);
            case ResizableCapacityLinkedBlockIngQueue:
            default:
                return new ResizableCapacityLinkedBlockIngQueue(capacity);
        }
    }


}
