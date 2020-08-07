package cn.fxbin.bubble.fireworks.plugin.lock.aop.support;

import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LockKeyGenerator
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/14 16:58
 */
public class LockKeyGenerator {

    /**
     * Generate a key for the given package and method.
     * @param method a single method on a class or interface
     * @param lockAction the annotation of {@see com.zichan360.framework.lock.annotation.Lock}
     * @return a generated key
     */
    public Object generate(Method method, LockAction lockAction) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName());
        sb.append(".").append(method.getName()).append(".");

        if(lockAction.keys().length > 0 && !lockAction.keys()[0].isEmpty()) {
            List<String> keys = new ArrayList<>(Arrays.asList(lockAction.keys()));
            String keysStr = StringUtils.collectionToDelimitedString(keys, ".", "", "");
            sb.append(keysStr);
        }
        return sb.toString();
    }

}
