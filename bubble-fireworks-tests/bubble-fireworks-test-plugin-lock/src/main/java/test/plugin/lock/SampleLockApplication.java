package test.plugin.lock;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Resource
    private LockAnnotationService lockAnnotationService;

    public static void main(String[] args) {
        SpringApplication.run(SampleLockApplication.class, args);
    }


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

        User user = new User();
        user.setId(999);
        User.Test test = new User.Test();
        test.setNumber("123456789");
        user.setTest(test);

        // spring el 表达式测试
        lockAnnotationService.spel1(user);

        // key 生成策略降级测试
        lockAnnotationService.spel2(user);
    }

}


