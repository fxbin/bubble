package test.plugin.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SampleLoggingApplication
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/8 17:23
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"test", "cn.fxbin"})
public class SampleLoggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleLoggingApplication.class, args);
    }


}
