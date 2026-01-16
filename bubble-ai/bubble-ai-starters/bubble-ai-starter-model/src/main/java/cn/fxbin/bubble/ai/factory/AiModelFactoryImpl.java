package cn.fxbin.bubble.ai.factory;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.constants.AiModelConstants;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.factory.creator.*;
import cn.fxbin.bubble.ai.factory.adapter.SpringAiAgentScopeAdapter;
import cn.fxbin.bubble.ai.manager.AiModelDefaults;
import cn.fxbin.bubble.ai.token.TokenCountingChatModel;
import cn.fxbin.bubble.ai.token.TokenUsageRecorder;
import cn.fxbin.bubble.ai.util.SensitiveDataUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GeminiChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.retry.support.RetryTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AI 模型工厂实现
 * <p>负责创建和管理AI模型实例，支持缓存、Token计数等功能</p>
 *
 * @author fxbin
 */
@Slf4j
public class AiModelFactoryImpl implements AiModelFactory {

    private record CacheKey(String platform, String baseUrl, String apiKeyHash, String model, Double temperature, Integer topK, Double topP) {
    }

    private final BubbleAiProperties properties;
    private final TokenUsageRecorder tokenUsageRecorder;
    private final TokenCountEstimator tokenCountEstimator;

    // Spring AI Native Models Providers
    private final ObjectProvider<OpenAiChatModel> openAiChatModelProvider;
    private final ObjectProvider<OllamaChatModel> ollamaChatModelProvider;
    private final ObjectProvider<AnthropicChatModel> anthropicChatModelProvider;
    private final ObjectProvider<VertexAiGeminiChatModel> geminiChatModelProvider;
    private final ObjectProvider<DeepSeekChatModel> deepSeekChatModelProvider;
    private final ObjectProvider<ZhiPuAiChatModel> zhipuAiChatModelProvider;
    private final ObjectProvider<MiniMaxChatModel> minimaxChatModelProvider;

    // Spring AI Native Embedding Models Providers
    private final ObjectProvider<OpenAiEmbeddingModel> openAiEmbeddingModelProvider;
    private final ObjectProvider<OllamaEmbeddingModel> ollamaEmbeddingModelProvider;
    private final ObjectProvider<ZhiPuAiEmbeddingModel> zhipuAiEmbeddingModelProvider;
    private final ObjectProvider<MiniMaxEmbeddingModel> minimaxEmbeddingModelProvider;
    private final ObjectProvider<AzureOpenAiEmbeddingModel> azureOpenAiEmbeddingModelProvider;

    private final ConcurrentMap<CacheKey, ChatModel> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<CacheKey, EmbeddingModel> embeddingCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> CACHE_KEY_CACHE = new ConcurrentHashMap<>();

    private final Map<AiPlatformEnum, AiModelCreator> creators = new EnumMap<>(AiPlatformEnum.class);

    public AiModelFactoryImpl(BubbleAiProperties properties,
                              TokenUsageRecorder tokenUsageRecorder,
                              TokenCountEstimator tokenCountEstimator,
                              ObjectProvider<OpenAiChatModel> openAiChatModelProvider,
                              ObjectProvider<OllamaChatModel> ollamaChatModelProvider,
                              ObjectProvider<AnthropicChatModel> anthropicChatModelProvider,
                              ObjectProvider<VertexAiGeminiChatModel> geminiChatModelProvider,
                              ObjectProvider<DeepSeekChatModel> deepSeekChatModelProvider,
                              ObjectProvider<ZhiPuAiChatModel> zhipuAiChatModelProvider,
                              ObjectProvider<MiniMaxChatModel> minimaxChatModelProvider,
                              ObjectProvider<OpenAiEmbeddingModel> openAiEmbeddingModelProvider,
                              ObjectProvider<OllamaEmbeddingModel> ollamaEmbeddingModelProvider,
                              ObjectProvider<ZhiPuAiEmbeddingModel> zhipuAiEmbeddingModelProvider,
                              ObjectProvider<MiniMaxEmbeddingModel> minimaxEmbeddingModelProvider,
                              ObjectProvider<AzureOpenAiEmbeddingModel> azureOpenAiEmbeddingModelProvider,
                              ObjectProvider<ToolCallingManager> toolCallingManagerProvider,
                              ObjectProvider<ObservationRegistry> observationRegistryProvider,
                              ObjectProvider<RetryTemplate> retryTemplateProvider) {
        this.properties = properties;
        this.tokenUsageRecorder = tokenUsageRecorder;
        this.tokenCountEstimator = tokenCountEstimator;
        this.openAiChatModelProvider = openAiChatModelProvider;
        this.ollamaChatModelProvider = ollamaChatModelProvider;
        this.anthropicChatModelProvider = anthropicChatModelProvider;
        this.geminiChatModelProvider = geminiChatModelProvider;
        this.deepSeekChatModelProvider = deepSeekChatModelProvider;
        this.zhipuAiChatModelProvider = zhipuAiChatModelProvider;
        this.minimaxChatModelProvider = minimaxChatModelProvider;
        this.openAiEmbeddingModelProvider = openAiEmbeddingModelProvider;
        this.ollamaEmbeddingModelProvider = ollamaEmbeddingModelProvider;
        this.zhipuAiEmbeddingModelProvider = zhipuAiEmbeddingModelProvider;
        this.minimaxEmbeddingModelProvider = minimaxEmbeddingModelProvider;
        this.azureOpenAiEmbeddingModelProvider = azureOpenAiEmbeddingModelProvider;

        ToolCallingManager toolCallingManager = toolCallingManagerProvider.getIfAvailable();
        ObservationRegistry observationRegistry = observationRegistryProvider.getIfAvailable();
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryUtils.DEFAULT_RETRY_TEMPLATE);

        // Initialize Creators
        this.creators.put(AiPlatformEnum.OPENAI, new OpenAiModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.OLLAMA, new OllamaModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.ANTHROPIC, new AnthropicModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.DEEPSEEK, new DeepSeekModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.ZHIPU, new ZhipuAiModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.MINIMAX, new MinimaxModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.SILICONFLOW, new SiliconFlowModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.DASHSCOPE, new DashScopeModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
        this.creators.put(AiPlatformEnum.AZURE_OPENAI, new AzureOpenAiModelCreator(properties, toolCallingManager, observationRegistry, retryTemplate));
    }

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url) {
        return getOrCreateChatModel(platform, apiKey, url, null, null, null);
    }

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature) {
        return getOrCreateChatModel(platform, apiKey, url, model, temperature, null);
    }

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK) {
        return getOrCreateChatModel(platform, apiKey, url, model, temperature, topK, null);
    }

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK, Double topP) {
        Objects.requireNonNull(platform, "platform must not be null");

        if (StringUtils.isBlank(apiKey) && StringUtils.isBlank(url) && StringUtils.isBlank(model) && temperature == null && topK == null && topP == null) {
            return getDefaultChatModel(platform);
        }

        ChatModel defaultModel = tryGetDefaultChatModel(platform);
        if (defaultModel != null && StringUtils.isBlank(apiKey) && StringUtils.isBlank(url) && StringUtils.isBlank(model) && temperature == null && topK == null && topP == null) {
            return defaultModel;
        }

        String resolvedModel = resolveDefaultModel(platform, model);
        Double resolvedTemperature = temperature != null ? temperature : AiModelConstants.Model.DEFAULT_TEMPERATURE;
        Integer resolvedTopK = topK;
        Double resolvedTopP = topP;

        ChatModel created = getOrCreateFromCache(platform, apiKey, url, resolvedModel, resolvedTemperature, resolvedTopK, resolvedTopP);
        return wrapTokenCountingIfNecessary(platform.getCode(), resolvedModel, created);
    }

    @Override
    public void removeChatModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK, Double topP) {
        Objects.requireNonNull(platform, "platform must not be null");

        String resolvedModel = resolveDefaultModel(platform, model);
        Double resolvedTemperature = temperature != null ? temperature : AiModelConstants.Model.DEFAULT_TEMPERATURE;
        Integer resolvedTopK = topK;
        Double resolvedTopP = topP;

        CacheKey key = new CacheKey(
                platform.getCode(),
                StringUtils.blankToDefault(url, ""),
                hashApiKey(apiKey),
                resolvedModel,
                resolvedTemperature,
                resolvedTopK,
                resolvedTopP
        );

        cache.remove(key);
        log.info("Removed ChatModel from cache for platform: {}, model: {}", platform, resolvedModel);
    }

    @Override
    public ChatModel getDefaultChatModel(AiPlatformEnum platform) {
        Objects.requireNonNull(platform, "platform must not be null");

        ChatModel model = tryGetDefaultChatModel(platform);

        if (model == null) {
            throw new IllegalStateException("No default ChatModel found for platform: " + platform);
        }

        return wrapTokenCountingIfNecessary(platform.getCode(), "default", model);
    }

    @Override
    public ChatModel getChatModel(String modelId) {
        if (StringUtils.isEmpty(modelId)) {
            throw new IllegalArgumentException("modelId must not be blank");
        }

        BubbleAiProperties.ProviderConfig config = properties.getProviders().get(modelId);
        if (config == null) {
            throw new IllegalArgumentException("No provider config found for id: " + modelId);
        }

        return getOrCreateChatModel(
                config.getPlatform(),
                config.getApiKey(),
                config.getBaseUrl(),
                config.getModel(),
                config.getTemperature(),
                config.getTopK(),
                config.getTopP()
        );
    }

    @Override
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url) {
        return getOrCreateEmbeddingModel(platform, apiKey, url, null, null);
    }

    @Override
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model) {
        return getOrCreateEmbeddingModel(platform, apiKey, url, model, null);
    }

    @Override
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model, Integer dimensions) {
        Objects.requireNonNull(platform, "platform must not be null");

        if (StringUtils.isBlank(apiKey) && StringUtils.isBlank(url) && StringUtils.isBlank(model) && dimensions == null) {
            return getDefaultEmbeddingModel(platform);
        }

        EmbeddingModel defaultModel = tryGetDefaultEmbeddingModel(platform);
        if (defaultModel != null && StringUtils.isBlank(apiKey) && StringUtils.isBlank(url) && StringUtils.isBlank(model) && dimensions == null) {
            return defaultModel;
        }

        String resolvedModel = resolveDefaultModel(platform, model);

        return getOrCreateEmbeddingFromCache(platform, apiKey, url, resolvedModel, dimensions);
    }

    @Override
    public EmbeddingModel getDefaultEmbeddingModel(AiPlatformEnum platform) {
        Objects.requireNonNull(platform, "platform must not be null");

        EmbeddingModel model = tryGetDefaultEmbeddingModel(platform);

        if (model == null) {
            throw new IllegalStateException("No default EmbeddingModel found for platform: " + platform);
        }

        return model;
    }

    @Override
    public EmbeddingModel getEmbeddingModel(String modelId) {
        if (StringUtils.isEmpty(modelId)) {
            throw new IllegalArgumentException("modelId must not be blank");
        }

        BubbleAiProperties.EmbeddingProviderConfig config = properties.getEmbeddingProviders().get(modelId);
        if (config == null) {
            throw new IllegalArgumentException("No embedding provider config found for id: " + modelId);
        }

        return getOrCreateEmbeddingModel(
                config.getPlatform(),
                config.getApiKey(),
                config.getBaseUrl(),
                config.getModel(),
                config.getDimensions()
        );
    }

    private ChatModel tryGetDefaultChatModel(AiPlatformEnum platform) {
        return switch (platform) {
            case OPENAI -> openAiChatModelProvider.getIfAvailable();
            case OLLAMA -> ollamaChatModelProvider.getIfAvailable();
            case ANTHROPIC -> anthropicChatModelProvider.getIfAvailable();
            case GEMINI -> geminiChatModelProvider.getIfAvailable();
            case DEEPSEEK -> deepSeekChatModelProvider.getIfAvailable();
            case ZHIPU -> zhipuAiChatModelProvider.getIfAvailable();
            case MINIMAX -> minimaxChatModelProvider.getIfAvailable();
            case SILICONFLOW -> openAiChatModelProvider.getIfAvailable();
            case DASHSCOPE -> null;
            case AZURE_OPENAI -> null;
        };
    }

    private EmbeddingModel tryGetDefaultEmbeddingModel(AiPlatformEnum platform) {
        return switch (platform) {
            case OPENAI -> openAiEmbeddingModelProvider.getIfAvailable();
            case OLLAMA -> ollamaEmbeddingModelProvider.getIfAvailable();
            case ANTHROPIC -> null;
            case GEMINI -> null;
            case DEEPSEEK -> null;
            case ZHIPU -> zhipuAiEmbeddingModelProvider.getIfAvailable();
            case MINIMAX -> minimaxEmbeddingModelProvider.getIfAvailable();
            case SILICONFLOW -> null;
            case DASHSCOPE -> null;
            case AZURE_OPENAI -> azureOpenAiEmbeddingModelProvider.getIfAvailable();
        };
    }

    private ChatModel getOrCreateFromCache(AiPlatformEnum platform, String apiKey, String url, String resolvedModel, Double resolvedTemperature, Integer resolvedTopK, Double resolvedTopP) {
        CacheKey key = new CacheKey(
                platform.getCode(),
                StringUtils.blankToDefault(url, ""),
                hashApiKey(apiKey),
                resolvedModel,
                resolvedTemperature,
                resolvedTopK,
                resolvedTopP
        );

        return cache.computeIfAbsent(key, ignored -> createChatModel(platform, apiKey, url, resolvedModel, resolvedTemperature, resolvedTopK, resolvedTopP));
    }

    private EmbeddingModel getOrCreateEmbeddingFromCache(AiPlatformEnum platform, String apiKey, String url, String resolvedModel, Integer dimensions) {
        CacheKey key = new CacheKey(
                platform.getCode(),
                StringUtils.blankToDefault(url, ""),
                hashApiKey(apiKey),
                resolvedModel,
                null,
                dimensions,
                null
        );

        return embeddingCache.computeIfAbsent(key, ignored -> createEmbeddingModel(platform, apiKey, url, resolvedModel, dimensions));
    }

    private String resolveDefaultModel(AiPlatformEnum platform, String model) {
        return AiModelDefaults.resolveModelName(platform, model);
    }

    private ChatModel wrapTokenCountingIfNecessary(String platform, String modelName, ChatModel model) {
        if (model instanceof TokenCountingChatModel) {
            return model;
        }
        if (properties.getTokenCounting() == null || !properties.getTokenCounting().isEnabled()) {
            return model;
        }
        boolean streamEstimationEnabled = properties.getTokenCounting() != null && properties.getTokenCounting().isStreamEstimationEnabled();
        return new TokenCountingChatModel(model, tokenUsageRecorder, tokenCountEstimator, platform, StringUtils.blankToDefault(modelName, "default"), streamEstimationEnabled);
    }

    private static String hashApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            return "";
        }
        return CACHE_KEY_CACHE.computeIfAbsent(apiKey, key -> {
            if (CACHE_KEY_CACHE.size() >= AiModelConstants.Cache.MAX_CACHE_KEY_CACHE_SIZE) {
                CACHE_KEY_CACHE.clear();
                log.debug("Cache key cache cleared due to size limit");
            }
            try {
                MessageDigest digest = MessageDigest.getInstance(AiModelConstants.Hash.DEFAULT_ALGORITHM);
                byte[] hashed = digest.digest(key.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder(hashed.length * 2);
                for (byte b : hashed) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to hash apiKey", e);
            }
        });
    }

    private ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String url, String modelName, Double temperature, Integer topK, Double topP) {
        log.info("Creating ChatModel for platform: {}, API Key: {}, Base URL: {}, Model: {}",
                platform,
                SensitiveDataUtils.maskApiKey(apiKey),
                SensitiveDataUtils.maskUrl(url),
                modelName);

        if (platform == AiPlatformEnum.GEMINI) {
            ChatModel injected = geminiChatModelProvider.getIfAvailable();
            if (injected != null) {
                return injected;
            }
            throw new IllegalStateException("GEMINI dynamic creation is not supported. Please configure a VertexAiGeminiChatModel bean.");
        }

        AiModelCreator creator = creators.get(platform);
        if (creator != null) {
            return creator.createChatModel(platform, apiKey, url, modelName, temperature, topK, topP);
        }

        throw new UnsupportedOperationException("No ChatModel creator found for platform: " + platform);
    }

    private EmbeddingModel createEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model, Integer dimensions) {
        log.info("Creating EmbeddingModel for platform: {}, API Key: {}, Base URL: {}, Model: {}",
                platform,
                SensitiveDataUtils.maskApiKey(apiKey),
                SensitiveDataUtils.maskUrl(url),
                model);

        AiModelCreator creator = creators.get(platform);
        if (creator != null) {
            return creator.createEmbeddingModel(platform, apiKey, url, model, dimensions);
        }

        throw new UnsupportedOperationException("No EmbeddingModel creator found for platform: " + platform);
    }

    @Override
    public io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url) {
        return getOrCreateAgentScopeModel(platform, apiKey, url, null, null);
    }

    @Override
    public io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature) {
        return getOrCreateAgentScopeModel(platform, apiKey, url, model, temperature, null);
    }

    @Override
    public io.agentscope.core.model.Model getOrCreateAgentScopeModel(AiPlatformEnum platform, String apiKey, String url, String model, Double temperature, Integer topK, Double topP) {
        // 1. 解析模型名称
        String resolvedModel = resolveDefaultModel(platform, model);

        // 2. 构建通用配置
        GenerateOptions.Builder optionsBuilder = GenerateOptions.builder();
        if (StringUtils.isNotBlank(apiKey)) {
            optionsBuilder.apiKey(apiKey);
        }
        if (StringUtils.isNotBlank(url)) {
            optionsBuilder.baseUrl(url);
        }
        if (StringUtils.isNotBlank(resolvedModel)) {
            optionsBuilder.modelName(resolvedModel);
        }
        if (temperature != null) {
            optionsBuilder.temperature(temperature);
        }
        if (topK != null) {
            optionsBuilder.topK(topK);
        }
        if (topP != null) {
            optionsBuilder.topP(topP);
        }

        GenerateOptions defaultOptions = optionsBuilder.build();

        // 3. 优先尝试原生 AgentScope 实现
        try {
            switch (platform) {
                case OPENAI:
                    return io.agentscope.core.model.OpenAIChatModel.builder()
                            .apiKey(apiKey)
                            .baseUrl(url)
                            .modelName(resolvedModel)
                            .generateOptions(defaultOptions)
                            .build();
                case DASHSCOPE:
                    return DashScopeChatModel.builder()
                            .apiKey(apiKey)
                            .baseUrl(url)
                            .modelName(resolvedModel)
                            .defaultOptions(defaultOptions)
                            .build();
                case GEMINI:
                    return GeminiChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(resolvedModel)
                            .defaultOptions(defaultOptions)
                            .build();
                case OLLAMA:
                    return io.agentscope.core.model.OllamaChatModel.builder()
                            .baseUrl(url)
                            .modelName(resolvedModel)
                            .defaultOptions(io.agentscope.core.model.ollama.OllamaOptions.fromGenerateOptions(defaultOptions))
                            .build();
                case ANTHROPIC:
                    return io.agentscope.core.model.AnthropicChatModel.builder()
                            .apiKey(apiKey)
                            .baseUrl(url)
                            .modelName(resolvedModel)
                            .defaultOptions(defaultOptions)
                            .build();
            }
        } catch (Exception e) {
            log.warn("Failed to create native AgentScope model for platform: {}, fallback to Spring AI adapter. Error: {}", platform, e.getMessage());
        }

        // 4. 回退到 Spring AI 适配器
        ChatModel springAiModel = getOrCreateChatModel(platform, apiKey, url, model, temperature, topK, topP);
        return new SpringAiAgentScopeAdapter(springAiModel, resolvedModel);
    }
}
