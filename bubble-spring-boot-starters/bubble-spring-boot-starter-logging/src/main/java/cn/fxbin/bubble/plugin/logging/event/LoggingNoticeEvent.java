package cn.fxbin.bubble.plugin.logging.event;

import cn.fxbin.bubble.plugin.logging.model.BubbleFireworksLogging;
import org.springframework.context.ApplicationEvent;

/**
 * LoggingNoticeEvent
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 14:11
 */
public class LoggingNoticeEvent extends ApplicationEvent {

    private static final long serialVersionUID = -5560571501086672853L;


    private final BubbleFireworksLogging fireworksLogging;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public LoggingNoticeEvent(Object source) {
        super(source);
        this.fireworksLogging = (BubbleFireworksLogging) source;
    }

    public BubbleFireworksLogging getFireworksLogging() {
        return fireworksLogging;
    }
}
