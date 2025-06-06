package cn.fxbin.bubble.plugin.satoken.model;

import cn.dev33.satoken.context.model.SaStorage;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * SaStorageForTtl
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/5 9:54
 */
public class SaStorageForTtl implements SaStorage {
    private final TransmittableThreadLocal<Map<String, Object>> storage = TransmittableThreadLocal.withInitial(HashMap::new);

    @Override
    public Object getSource() {
        return this;
    }

    @Override
    public Object get(String key) {
        return storage.get().get(key);
    }

    @Override
    public SaStorage set(String key, Object value) {
        storage.get().put(key, value);
        return this;
    }

    @Override
    public SaStorage delete(String key) {
        storage.get().remove(key);
        return this;
    }

    public void clear() {
        storage.remove();
    }
}
