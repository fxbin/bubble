package test.plugin.lock;

import cn.fxbin.bubble.fireworks.core.util.ThreadUtils;
import cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction;
import cn.fxbin.bubble.fireworks.plugin.lock.model.LockKeyGeneratorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LockAnnotationService
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 18:35
 */
@Slf4j
@Service
public class LockAnnotationService {

    @LockAction(keys = {"aaa"}, waitTime = 1000)
    public void aaa() {
        System.out.println("dididi");
        ThreadUtils.sleep(3000);
    }

    @LockAction(keys = "#user.test.number", keyGeneratorType = LockKeyGeneratorStrategy.Expression)
    public void spel1(User user){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("exp", e);
        }
    }

    @LockAction(keyGeneratorType = LockKeyGeneratorStrategy.Expression)
    public void spel2(User user){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("exp", e);
        }
    }




}
