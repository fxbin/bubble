package cn.fxbin.bubble.plugin.dynamic.threadpool.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * RefreshThreadConfigEvent
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/9 11:15
 */
public class ThreadContextRefreshEvent extends ApplicationEvent {

    private static final long serialVersionUID = -7762476853205271683L;

    private final Map<String, Object> propertyMap;

    public ThreadContextRefreshEvent(Map<String, Object> propertyMap) {
        super(propertyMap);
        this.propertyMap = propertyMap;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }
}
