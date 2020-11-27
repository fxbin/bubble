package cn.fxbin.bubble.fireworks.core.util;

import java.sql.Timestamp;
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

    INSTANCE(1);

    private final long period;
    private final AtomicLong nowTime;
    private boolean started = false;
    private ScheduledExecutorService executorService;

    SystemClock(long period) {
        this.period = period;
        this.nowTime = new AtomicLong(System.currentTimeMillis());
    }

    /**
     * The initialize scheduled executor service
     */
    public void initialize() {
        if (started) {
            return;
        }

        this.executorService = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "system-clock");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(() -> nowTime.set(System.currentTimeMillis()),
                this.period, this.period, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
        started = true;
    }

    /**
     * The get current time milliseconds
     *
     * @return long time
     */
    public long currentTimeMillis() {
        return started ? nowTime.get() : System.currentTimeMillis();
    }

    /**
     * The get string current time
     *
     * @return string time
     */
    public String currentTime() {
        return new Timestamp(currentTimeMillis()).toString();
    }

    /**
     * The destroy of executor service
     */
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

}
