package cn.fxbin.bubble.ai.token;

/**
 * Token Usage Recorder Interface
 *
 * @author fxbin
 */
public interface TokenUsageRecorder {

    /**
     * Record token usage
     *
     * @param context Token usage context
     */
    void record(TokenUsageContext context);

}
