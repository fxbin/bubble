package cn.fxbin.bubble.ai.manager;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.ai.service.AiModelConfigService;
import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.domain.dto.AiModelInfo;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AiModelManager 实现
 *
 * @author fxbin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelManagerImpl implements AiModelManager {

    private final BubbleAiProperties properties;
    private final AiModelFactory aiModelFactory;
    private final ObjectProvider<AiModelConfigService> aiModelConfigServiceProvider;

    @Override
    public List<AiModelInfo> listAvailableModels() {
        List<AiModelInfo> models = new ArrayList<>();

        if (properties.getProviders() != null) {
            properties.getProviders().forEach((key, config) -> {
                try {
                    String modelName = AiModelDefaults.resolveModelName(config.getPlatform(), config.getModel());
                    models.add(new AiModelInfo(
                            key,
                            StrUtil.blankToDefault(key, modelName),
                            modelName,
                            config.getDescription(),
                            config.getPlatform().name(),
                            "CONFIG"
                    ));
                } catch (Exception e) {
                    log.warn("Failed to load model config for key: {}, error: {}", key, e.getMessage());
                }
            });
        }

        AiModelConfigService service = aiModelConfigServiceProvider.getIfAvailable();
        if (service != null) {
            try {
                List<AiModelConfig> dbConfigs = service.list();
                if (dbConfigs != null) {
                    dbConfigs.stream()
                            .filter(c -> Boolean.TRUE.equals(c.getEnabled()))
                            .forEach(c -> {
                                try {
                                    String modelName = AiModelDefaults.resolveModelName(c.getPlatform(), c.getModel());
                                    models.add(new AiModelInfo(
                                            c.getId(),
                                            c.getConfigName(),
                                            modelName,
                                            c.getDescription(),
                                            c.getPlatform().name(),
                                            "DATABASE"
                                    ));
                                } catch (Exception e) {
                                    log.warn("Failed to process database model config: {}, error: {}", c.getConfigName(), e.getMessage());
                                }
                            });
                }
            } catch (Exception e) {
                log.error("Failed to load models from database", e);
            }
        }

        log.debug("Loaded {} available models", models.size());
        return models;
    }

    @Override
    public ChatModel getChatModel(String modelId) {
        if (StrUtil.isBlank(modelId)) {
            throw new IllegalArgumentException("Model ID must not be blank");
        }

        log.debug("Attempting to get ChatModel for modelId: {}", modelId);

        if (properties.getProviders() != null && properties.getProviders().containsKey(modelId)) {
            try {
                ChatModel model = aiModelFactory.getChatModel(modelId);
                log.debug("Successfully loaded ChatModel from config for modelId: {}", modelId);
                return model;
            } catch (Exception e) {
                log.error("Failed to load ChatModel from config for modelId: {}, error: {}", modelId, e.getMessage(), e);
                throw new IllegalStateException("Failed to load model from config: " + modelId, e);
            }
        }

        AiModelConfigService service = aiModelConfigServiceProvider.getIfAvailable();
        if (service != null) {
            try {
                ChatModel model = service.getChatModel(modelId);
                log.debug("Successfully loaded ChatModel from database for modelId: {}", modelId);
                return model;
            } catch (Exception e) {
                log.warn("Model ID {} lookup in database failed: {}", modelId, e.getMessage());
            }
        }

        log.error("Model not found: {}", modelId);
        throw new IllegalArgumentException("Model not found: " + modelId);
    }
}
