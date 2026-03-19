package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.domain.dto.AiModelTestResult;
import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.factory.FailoverChatModel;
import cn.fxbin.bubble.ai.manager.AiModelDefaults;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import cn.fxbin.bubble.ai.mapper.AiModelGroupMapper;
import cn.fxbin.bubble.ai.service.AiModelConfigService;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * AI model configuration service implementation.
 *
 * @author fxbin
 */
@Slf4j
@RequiredArgsConstructor
public class AiModelConfigServiceImpl extends ServiceImpl<AiModelConfigMapper, AiModelConfig> implements AiModelConfigService {

    private final AiModelFactory aiModelFactory;

    private final AiModelConfigMapper aiModelConfigMapper;

    private final AiModelGroupMapper aiModelGroupMapper;

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
        AiModelConfig config;
        if (resolvedId != null) {
            config = queryEnabledById(resolvedId);
            if (config == null) {
                config = queryEnabledByConfigName(modelId);
            }
        } else {
            config = queryEnabledByConfigName(modelId);
        }

        if (config != null) {
            if (StrUtil.isNotBlank(config.getGroupId()) && !Boolean.FALSE.equals(config.getFallbackToGroupEnabled())) {
                AiModelGroup group = queryEnabledGroup(config.getGroupId());
                if (group != null && !Boolean.FALSE.equals(group.getFailoverEnabled())) {
                    return buildGroupFailoverChatModel(modelId, group, config);
                }
                return buildGroupFailoverChatModel(modelId, config.getGroupId(), config, null);
            }
            return buildChatModel(config, modelId);
        }

        AiModelGroup group = queryEnabledGroup(modelId);
        if (group != null) {
            return buildGroupFailoverChatModel(modelId, group, null);
        }

        List<AiModelConfig> legacyGroupConfigs = queryEnabledByGroupId(modelId);
        if (!legacyGroupConfigs.isEmpty()) {
            return buildGroupFailoverChatModel(modelId, modelId, null, null);
        }

        throw new IllegalArgumentException("AI Model Config not found or disabled: " + modelId);
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
            log.warn("模型配置检测失败: configName={}, platform={}", aiModelConfig.getConfigName(), platform, e);
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

    private AiModelConfig queryEnabledById(Long id) {
        LambdaQueryWrapper<AiModelConfig> queryWrapper = Wrappers.lambdaQuery(AiModelConfig.class)
                .eq(AiModelConfig::getEnabled, true)
                .eq(AiModelConfig::getId, id);
        return aiModelConfigMapper.selectOne(queryWrapper);
    }

    private AiModelConfig queryEnabledByConfigName(String configName) {
        LambdaQueryWrapper<AiModelConfig> queryWrapper = Wrappers.lambdaQuery(AiModelConfig.class)
                .eq(AiModelConfig::getEnabled, true)
                .eq(AiModelConfig::getConfigName, configName);
        return aiModelConfigMapper.selectOne(queryWrapper);
    }

    private List<AiModelConfig> queryEnabledByGroupId(String groupId) {
        LambdaQueryWrapper<AiModelConfig> queryWrapper = Wrappers.lambdaQuery(AiModelConfig.class)
                .eq(AiModelConfig::getEnabled, true)
                .eq(AiModelConfig::getGroupId, groupId)
                .orderByAsc(AiModelConfig::getPriority)
                .orderByAsc(AiModelConfig::getId);
        List<AiModelConfig> configs = aiModelConfigMapper.selectList(queryWrapper);
        return configs != null ? configs : List.of();
    }

    private AiModelGroup queryEnabledGroup(String identifier) {
        if (StrUtil.isBlank(identifier)) {
            return null;
        }
        Long resolvedId = parseLongOrNull(identifier);
        if (resolvedId != null) {
            AiModelGroup group = aiModelGroupMapper.selectOne(Wrappers.lambdaQuery(AiModelGroup.class)
                    .eq(AiModelGroup::getEnabled, true)
                    .eq(AiModelGroup::getId, resolvedId));
            if (group != null) {
                return group;
            }
        }
        return aiModelGroupMapper.selectOne(Wrappers.lambdaQuery(AiModelGroup.class)
                .eq(AiModelGroup::getEnabled, true)
                .eq(AiModelGroup::getGroupCode, identifier));
    }

    private ChatModel buildGroupFailoverChatModel(String targetId, AiModelGroup group, AiModelConfig selectedConfig) {
        return buildGroupFailoverChatModel(targetId, group.getGroupCode(), selectedConfig, group);
    }

    private ChatModel buildGroupFailoverChatModel(String targetId, String groupId, AiModelConfig selectedConfig, AiModelGroup group) {
        List<AiModelConfig> groupConfigs = queryEnabledByGroupId(groupId);
        Assert.notEmpty(groupConfigs, "AI Model Group not found or has no enabled members: {}", groupId);

        Set<Long> orderedIds = new LinkedHashSet<>();
        if (selectedConfig != null && selectedConfig.getId() != null) {
            orderedIds.add(selectedConfig.getId());
        }

        groupConfigs.stream()
                .sorted(Comparator
                        .comparing((AiModelConfig config) -> config.getPriority() != null ? config.getPriority() : Integer.MAX_VALUE)
                        .thenComparing(config -> config.getId() != null ? config.getId() : Long.MAX_VALUE))
                .map(AiModelConfig::getId)
                .forEach(orderedIds::add);

        List<FailoverChatModel.Candidate> candidates = new ArrayList<>();
        for (Long configId : orderedIds) {
            AiModelConfig config = groupConfigs.stream()
                    .filter(item -> configId.equals(item.getId()))
                    .findFirst()
                    .orElse(null);
            if (config == null) {
                continue;
            }
            candidates.add(new FailoverChatModel.Candidate(resolveCandidateId(config), buildChatModel(config, String.valueOf(configId))));
        }

        Assert.notEmpty(candidates, "AI Model Group not found or has no enabled members: {}", groupId);
        if (candidates.size() == 1) {
            return candidates.get(0).getModel();
        }
        long cooldownMillis = resolveCooldownMillis(group);
        return new FailoverChatModel(targetId, candidates, cooldownMillis);
    }

    private ChatModel buildChatModel(AiModelConfig config, String modelId) {
        Assert.notNull(config, "AI Model Config not found or disabled: {}", modelId);

        AiPlatformEnum platform = config.getPlatform();
        Assert.notNull(platform, "Unsupported platform: {}", config.getPlatform());

        String originalModelName = config.getModel();
        String resolvedModelName = AiModelDefaults.resolveModelName(platform, originalModelName);

        if (StrUtil.isBlank(originalModelName) || "-".equals(originalModelName)) {
            log.info("AI Model Config [{}] uses default model: platform={}, modelName={}", modelId, platform, resolvedModelName);
        } else {
            log.info("AI Model Config [{}] uses configured model: platform={}, modelName={}", modelId, platform, resolvedModelName);
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

    private String resolveCandidateId(AiModelConfig config) {
        if (StrUtil.isNotBlank(config.getConfigName())) {
            return config.getConfigName();
        }
        return String.valueOf(config.getId());
    }

    private long resolveCooldownMillis(AiModelGroup group) {
        if (group == null || group.getCooldownSeconds() == null || group.getCooldownSeconds() <= 0) {
            return 30_000L;
        }
        return group.getCooldownSeconds() * 1000L;
    }
}
