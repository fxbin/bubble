package cn.fxbin.bubble.core.util.ttl;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * TtlThreadPoolTaskExecutor
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/22 17:54
 */
public class TtlThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    private static final long serialVersionUID = -8598415686869392804L;

    @Override
    public void execute(@NonNull Runnable task) {
        TtlRunnable ttlRunnable = TtlRunnable.get(task);
        assert ttlRunnable != null;
        super.execute(ttlRunnable);
    }

    @NonNull
    @Override
    public Future<?> submit(@NonNull Runnable task) {
        TtlRunnable ttlRunnable = TtlRunnable.get(task);
        assert ttlRunnable != null;
        return super.submit(ttlRunnable);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Callable<T> task) {
        TtlCallable<T> ttlCallable = TtlCallable.get(task);
        assert ttlCallable != null;
        return super.submit(ttlCallable);
    }

    @NonNull
    @Override
    public <T> CompletableFuture<T> submitCompletable(@NonNull Callable<T> task) {
        TtlCallable<T> ttlCallable = TtlCallable.get(task);
        assert ttlCallable != null;
        return super.submitCompletable(task);
    }

}
