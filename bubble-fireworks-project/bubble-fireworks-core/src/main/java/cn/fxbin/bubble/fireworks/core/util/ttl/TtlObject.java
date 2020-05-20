package cn.fxbin.bubble.fireworks.core.util.ttl;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * TtlObjectUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 19:16
 */
@UtilityClass
public class TtlObject {

    private final TransmittableThreadLocal<Object> LOCAL_CACHE = new TransmittableThreadLocal<>();

    /**
     * set current thread object instance
     *
     * @since 2020/5/20 11:28
     * @param object object instance
     */
    public void set(Object object) {
        LOCAL_CACHE.set(object);
    }

    /**
     * get current thread object instance
     *
     * @since 2020/5/20 11:28
     * @return java.lang.Object
     */
    public Object get() {
        return LOCAL_CACHE.get();
    }

    /**
     * remove remove current object instance
     *
     * @since 2020/5/20 11:29
     */
    public void remove() {
        LOCAL_CACHE.remove();
    }



}
