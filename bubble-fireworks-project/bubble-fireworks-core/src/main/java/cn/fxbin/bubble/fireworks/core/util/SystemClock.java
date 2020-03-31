package cn.fxbin.bubble.fireworks.core.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SystemClock
 *
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * <p>
 *      https://gitee.com/yu120/sequence
 * <p>
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）<p>
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道<p>
 * 后台定时更新时钟，JVM退出时，线程自动回收<p>
 * 10亿：43410,206,210.72815533980582%<p>
 * 1亿：4699,29,162.0344827586207%<p>
 * 1000万：480,12,40.0%<p>
 * 100万：50,10,5.0%<p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/20 17:36
 * @see System#currentTimeMillis()
 */
public enum SystemClock {

    // ====
    INSTANCE(1L, 1);

    private final AtomicLong now;

    SystemClock(long period, int corePoolSize) {
        this.now = new AtomicLong(System.currentTimeMillis());
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(corePoolSize, r -> {
            Thread t = new Thread(r, "system-clock");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    public long currentTimeMillis() {
        return now.get();
    }

}
