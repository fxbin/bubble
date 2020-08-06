package test.plugin.lock;

import cn.fxbin.bubble.fireworks.core.util.ThreadUtils;
import cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SampleLockApplication
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/4 16:49
 */
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class SampleLockApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SampleLockApplication.class, args);
    }


    @Resource
    private LockAnnotationService lockAnnotationService;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on err or
     */
    @Override
    public void run(String... args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> lockAnnotationService.aaa());
    }

}


