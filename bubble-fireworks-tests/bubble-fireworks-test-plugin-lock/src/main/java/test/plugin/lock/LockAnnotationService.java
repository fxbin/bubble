package test.plugin.lock;

import cn.fxbin.bubble.fireworks.core.util.ThreadUtils;
import cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction;
import org.springframework.stereotype.Service;

/**
 * LockAnnotationService
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 18:35
 */
@Service
public class LockAnnotationService {

    @LockAction(keys = {"aaa"}, waitTime = 1000)
    public void aaa() {
        System.out.println("dididi");
        ThreadUtils.sleep(3000);
    }



}
