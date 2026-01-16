package cn.fxbin.bubble.ai.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;

/**
 * 默认 Token 使用记录器（基于日志）
 * <p>将Token使用情况记录到日志中</p>
 *
 * @author fxbin
 */
@Slf4j
public class DefaultTokenUsageRecorder implements TokenUsageRecorder {

    @Override
    public void record(TokenUsageContext context) {
        if (context.usage() != null) {
            Usage usage = context.usage();
            log.info("Token 使用统计 - 平台: {}, 模型: {}, 详情: {}",
                    context.platform(),
                    context.model(),
                    usage.toString());
        } else {
            log.warn("Token 使用统计 - 平台: {}, 模型: {}, 使用信息缺失。", context.platform(), context.model());
        }
    }
}
