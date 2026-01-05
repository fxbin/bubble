package cn.fxbin.bubble.ai.service.impl;

import cn.fxbin.bubble.ai.manager.AiModelDefaults;
import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import cn.fxbin.bubble.ai.service.AiModelConfigService;
import cn.fxbin.bubble.core.exception.ServiceException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

/**
 * AI 模型配置服务实现
 *
 * @author fxbin
 */
@Slf4j
@RequiredArgsConstructor
public class AiModelConfigServiceImpl extends ServiceImpl<AiModelConfigMapper, AiModelConfig> implements AiModelConfigService {

    private final AiModelFactory aiModelFactory;

    private final AiModelConfigMapper aiModelConfigMapper;


    @Override
    public ChatModel getChatModel(String configName) {
        if (StrUtil.isBlank(configName)) {
            throw new IllegalArgumentException("configName must not be blank");
        }

        AiModelConfig config = aiModelConfigMapper.selectOne(
                Wrappers.lambdaQuery(AiModelConfig.class)
                        .eq(AiModelConfig::getConfigName, configName)
                        .eq(AiModelConfig::getEnabled, true)
        );

        Assert.notNull(config, "AI Model Config not found or disabled: {}", configName);

        AiPlatformEnum platform = config.getPlatform();
        Assert.notNull(platform, "Unsupported platform: {}", config.getPlatform());

        String originalModelName = config.getModel();
        String resolvedModelName = AiModelDefaults.resolveModelName(platform, originalModelName);

        if (StrUtil.isBlank(originalModelName) || "-".equals(originalModelName)) {
            log.info("AI Model Config [{}] 使用默认模型: platform={}, modelName={}", configName, platform, resolvedModelName);
        } else {
            log.info("AI Model Config [{}] 使用配置模型: platform={}, modelName={}", configName, platform, resolvedModelName);
        }

        return aiModelFactory.getOrCreateChatModel(
                platform,
                config.getApiKey(),
                config.getBaseUrl(),
                resolvedModelName,
                config.getTemperature(),
                config.getTopK()
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

}
