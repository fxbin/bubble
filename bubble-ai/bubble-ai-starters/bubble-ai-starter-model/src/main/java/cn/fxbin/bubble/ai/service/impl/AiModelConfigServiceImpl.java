package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.domain.dto.AiModelTestResult;
import cn.fxbin.bubble.ai.manager.AiModelDefaults;
import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import cn.fxbin.bubble.ai.service.AiModelConfigService;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * AI 模型配置服务实现
 * <p>提供AI模型配置的具体业务逻辑实现</p>
 *
 * @author fxbin
 */
@Slf4j
@RequiredArgsConstructor
public class AiModelConfigServiceImpl extends ServiceImpl<AiModelConfigMapper, AiModelConfig> implements AiModelConfigService {

    private final AiModelFactory aiModelFactory;

    private final AiModelConfigMapper aiModelConfigMapper;

    @Override
    public boolean removeById(Serializable id) {
        AiModelConfig config = getById(id);
        boolean success = super.removeById(id);
        if (success) {
            clearCache(config);
        }
        return success;
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        // Fetch before delete
        List<AiModelConfig> configs = listByIds((Collection<? extends Serializable>) list);
        boolean success = super.removeByIds(list);
        if (success && configs != null) {
            configs.forEach(this::clearCache);
        }
        return success;
    }

    @Override
    public boolean updateById(AiModelConfig entity) {
        AiModelConfig oldConfig = getById(entity.getId());
        boolean success = super.updateById(entity);
        if (success) {
            clearCache(oldConfig);
        }
        return success;
    }

    private void clearCache(AiModelConfig config) {
        if (config == null) {
            return;
        }
        AiPlatformEnum platform = config.getPlatform();
        if (platform == null) {
            return;
        }

        String resolvedModelName = AiModelDefaults.resolveModelName(platform, config.getModel());

        aiModelFactory.removeChatModel(
                platform,
                config.getApiKey(),
                config.getBaseUrl(),
                resolvedModelName,
                config.getTemperature(),
                config.getTopK(),
                config.getTopP()
        );
    }


    @Override
    public ChatModel getChatModel(String modelId) {
        if (StrUtil.isBlank(modelId)) {
            throw new IllegalArgumentException("modelId must not be blank");
        }

        Long resolvedId = parseLongOrNull(modelId);
        LambdaQueryWrapper<AiModelConfig> queryWrapper = Wrappers.lambdaQuery(AiModelConfig.class)
                .eq(AiModelConfig::getEnabled, true);
        if (resolvedId != null) {
            queryWrapper.and(w -> w.eq(AiModelConfig::getId, resolvedId).or().eq(AiModelConfig::getConfigName, modelId));
        } else {
            queryWrapper.eq(AiModelConfig::getConfigName, modelId);
        }
        AiModelConfig config = aiModelConfigMapper.selectOne(queryWrapper);

        Assert.notNull(config, "AI Model Config not found or disabled: {}", modelId);

        AiPlatformEnum platform = config.getPlatform();
        Assert.notNull(platform, "Unsupported platform: {}", config.getPlatform());

        String originalModelName = config.getModel();
        String resolvedModelName = AiModelDefaults.resolveModelName(platform, originalModelName);

        if (StrUtil.isBlank(originalModelName) || "-".equals(originalModelName)) {
            log.info("AI Model Config [{}] 使用默认模型: platform={}, modelName={}", modelId, platform, resolvedModelName);
        } else {
            log.info("AI Model Config [{}] 使用配置模型: platform={}, modelName={}", modelId, platform, resolvedModelName);
        }

        return aiModelFactory.getOrCreateChatModel(
                platform,
                config.getApiKey(),
                config.getBaseUrl(),
                resolvedModelName,
                config.getTemperature(),
                config.getTopK(),
                config.getTopP()
        );
    }

    @Override
    public void validateConfigNameUnique(String configName, Long id) {
        long count = count(Wrappers.lambdaQuery(AiModelConfig.class)
                .eq(AiModelConfig::getConfigName, configName)
                .ne(id != null, AiModelConfig::getId, id));
        if (count > 0) {
            throw new ServiceException("配置名称 " + configName + " 已存在");
        }
    }

    @Override
    public void createAiModel(AiModelConfig aiModelConfig) {
        validateConfigNameUnique(aiModelConfig.getConfigName(), null);
        save(aiModelConfig);
    }

    @Override
    public void updateAiModel(AiModelConfig aiModelConfig) {
        validateConfigNameUnique(aiModelConfig.getConfigName(), aiModelConfig.getId());
        updateById(aiModelConfig);
    }

    @Override
    public void removeAiModel(Long id) {
        removeById(id);
    }

    @Override
    public AiModelTestResult testAiModel(AiModelConfig aiModelConfig) {
        if (aiModelConfig == null) {
            return new AiModelTestResult(false, "模型配置不能为空", null, null, null, null);
        }
        AiPlatformEnum platform = aiModelConfig.getPlatform();
        if (platform == null) {
            return new AiModelTestResult(false, "模型平台不能为空", null, null, null, aiModelConfig.getId());
        }
        String resolvedModelName = AiModelDefaults.resolveModelName(platform, aiModelConfig.getModel());
        long startTime = System.currentTimeMillis();
        try {
            ChatModel chatModel = aiModelFactory.getOrCreateChatModel(
                    platform,
                    aiModelConfig.getApiKey(),
                    aiModelConfig.getBaseUrl(),
                    resolvedModelName,
                    aiModelConfig.getTemperature(),
                    aiModelConfig.getTopK(),
                    aiModelConfig.getTopP()
            );
            chatModel.call(new Prompt(new UserMessage("ping")));
            long latency = System.currentTimeMillis() - startTime;
            return new AiModelTestResult(true, "ok", resolvedModelName, platform.name(), latency, aiModelConfig.getId());
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            log.warn("模型配置检测失败: configName={}, platform={}", aiModelConfig.getConfigName(), platform);
            String message = StrUtil.blankToDefault(e.getMessage(), "模型检测失败");
            return new AiModelTestResult(false, message, resolvedModelName, platform.name(), latency, aiModelConfig.getId());
        } finally {
            aiModelFactory.removeChatModel(
                    platform,
                    aiModelConfig.getApiKey(),
                    aiModelConfig.getBaseUrl(),
                    resolvedModelName,
                    aiModelConfig.getTemperature(),
                    aiModelConfig.getTopK(),
                    aiModelConfig.getTopP()
            );
        }
    }

    @Override
    public AiModelTestResult testAiModelById(Long id) {
        if (id == null) {
            return new AiModelTestResult(false, "模型配置ID不能为空", null, null, null, null);
        }
        AiModelConfig config = getById(id);
        if (config == null) {
            return new AiModelTestResult(false, "模型配置不存在", null, null, null, id);
        }
        return testAiModel(config);
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
