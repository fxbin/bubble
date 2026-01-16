package cn.fxbin.bubble.ai.token;

/**
 * Token 使用记录器接口
 * <p>定义了记录Token使用情况的统一接口</p>
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
