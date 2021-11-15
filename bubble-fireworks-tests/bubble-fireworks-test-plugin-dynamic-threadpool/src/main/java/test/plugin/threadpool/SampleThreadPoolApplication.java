package test.plugin.threadpool;

import cn.fxbin.bubble.fireworks.plugin.dynamic.threadpool.support.ThreadPoolExecutorOperations;
import cn.hutool.core.thread.ThreadUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.util.Set;

/**
 * SampleThreadPoolApplication
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/10 11:45
 */
@SpringBootApplication
public class SampleThreadPoolApplication implements CommandLineRunner {

    @Resource
    private ThreadPoolExecutorOperations operations;

    public static void main(String[] args) {
        SpringApplication.run(SampleThreadPoolApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<String> keys = operations.keys();
        System.out.println(keys);


        for (int i = 0; i < 10000; i++) {

            int finalI = i;
            operations.get("test").execute(() -> {
                System.out.println(finalI);
                ThreadUtil.sleep(100);
            });
        }


        for (int i = 0; i < 10000; i++) {

            int finalI = i;
            operations.get("test2222").execute(() -> {
                System.out.println(finalI);
                ThreadUtil.sleep(100);
            });
        }

    }
}
