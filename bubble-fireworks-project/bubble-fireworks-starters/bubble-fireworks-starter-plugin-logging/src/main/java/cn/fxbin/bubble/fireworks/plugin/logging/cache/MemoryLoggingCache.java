package cn.fxbin.bubble.fireworks.plugin.logging.cache;

import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import cn.fxbin.bubble.fireworks.plugin.logging.model.BubbleFireworksLogging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MemoryLoggingCache
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 13:50
 */
public class MemoryLoggingCache implements LoggingCache {

    private static final ConcurrentMap<String, BubbleFireworksLogging> MEMORY_CACHE_LOGS = new ConcurrentHashMap<>();

    @Override
    public void cache(BubbleFireworksLogging fireworksLogging) {
        if (ObjectUtils.isNotEmpty(fireworksLogging)) {
            MEMORY_CACHE_LOGS.put(StringUtils.getUUID(), fireworksLogging);
        }
    }

    @Override
    public List<BubbleFireworksLogging> getLogs(Integer count) {
        return get(count);
    }

    @Override
    public List<BubbleFireworksLogging> getAllLogs() {
        return get(null);
    }

    /**
     * get
     *
     * @since 2020/5/19 14:00
     * @param count count number
     * @return java.util.List<cn.fxbin.bubble.fireworks.logging.model.BubbleFireworksLogging>
     */
    private List<BubbleFireworksLogging> get(Integer count) {
        List<BubbleFireworksLogging> logs = new ArrayList<>();;
        Iterator<String> logsKey = MEMORY_CACHE_LOGS.keySet().iterator();
        int index = 0;
        while (logsKey.hasNext()) {
            String key = logsKey.next();
            logs.add(MEMORY_CACHE_LOGS.get(key));
            MEMORY_CACHE_LOGS.remove(key);
            if (ObjectUtils.isNotEmpty(count) && index >= count - 1) {
                break;
            }
            index++;
        }
        return logs;
    }

}
