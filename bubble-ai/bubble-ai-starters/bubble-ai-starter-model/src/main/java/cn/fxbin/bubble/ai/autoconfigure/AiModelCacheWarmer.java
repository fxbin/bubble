package cn.fxbin.bubble.ai.autoconfigure;

import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;

/**
 * AI 模型缓存预热器
 * <p>在应用启动时预热模型缓存，避免首次访问延迟</p>
 *
 * @author fxbin
 */
@Slf4j
@RequiredArgsConstructor
@Order
@ConditionalOnProperty(
        prefix = "bubble.ai.cache",
        name = "preload-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AiModelCacheWarmer implements ApplicationListener<ApplicationReadyEvent> {

    private final BubbleAiProperties properties;
    private final AiModelFactory aiModelFactory;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("开始预热 AI 模型缓存...");
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        if (properties.getProviders() != null && !properties.getProviders().isEmpty()) {
            log.info("预热配置文件中的 {} 个 Chat 模型", properties.getProviders().size());
            for (String modelId : properties.getProviders().keySet()) {
                try {
                    aiModelFactory.getChatModel(modelId);
                    successCount++;
                    log.debug("预热 Chat 模型成功: {}", modelId);
                } catch (Exception e) {
                    failCount++;
                    log.warn("预热 Chat 模型失败: {}, 错误: {}", modelId, e.getMessage());
                }
            }
        }

        if (properties.getEmbeddingProviders() != null && !properties.getEmbeddingProviders().isEmpty()) {
            log.info("预热配置文件中的 {} 个 Embedding 模型", properties.getEmbeddingProviders().size());
            for (String modelId : properties.getEmbeddingProviders().keySet()) {
                try {
                    aiModelFactory.getEmbeddingModel(modelId);
                    successCount++;
                    log.debug("预热 Embedding 模型成功: {}", modelId);
                } catch (Exception e) {
                    failCount++;
                    log.warn("预热 Embedding 模型失败: {}, 错误: {}", modelId, e.getMessage());
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("AI 模型缓存预热完成，成功: {}, 失败: {}, 耗时: {}ms", successCount, failCount, duration);
    }
}
