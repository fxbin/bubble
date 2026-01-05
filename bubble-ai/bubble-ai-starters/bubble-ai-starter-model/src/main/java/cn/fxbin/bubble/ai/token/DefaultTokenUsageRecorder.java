package cn.fxbin.bubble.ai.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;

/**
 * Default Token Usage Recorder (Log based)
 *
 * @author fxbin
 */
@Slf4j
public class DefaultTokenUsageRecorder implements TokenUsageRecorder {

    @Override
    public void record(TokenUsageContext context) {
        if (context.usage() != null) {
            Usage usage = context.usage();
            // Using toString() to avoid method name issues if getter names changed
            // But trying standard getters first if possible, or just toString which is usually safe
            log.info("Token Usage - Platform: {}, Model: {}, Details: {}",
                    context.platform(),
                    context.model(),
                    usage.toString());
        } else {
            log.warn("Token Usage - Platform: {}, Model: {}, Usage info is missing.", context.platform(), context.model());
        }
    }
}
