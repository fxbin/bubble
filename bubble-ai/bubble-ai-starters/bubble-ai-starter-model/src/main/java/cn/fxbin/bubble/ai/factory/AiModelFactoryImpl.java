package cn.fxbin.bubble.ai.factory;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import cn.fxbin.bubble.ai.constants.AiModelConstants;
import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import cn.fxbin.bubble.ai.manager.AiModelDefaults;
import cn.fxbin.bubble.ai.token.TokenCountingChatModel;
import cn.fxbin.bubble.ai.token.TokenUsageRecorder;
import cn.fxbin.bubble.ai.util.SensitiveDataUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.retry.support.RetryTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AI 模型工厂实现
 *
 * @author fxbin
 */
@Slf4j
public class AiModelFactoryImpl implements AiModelFactory {

    private record CacheKey(String platform, String baseUrl, String apiKeyHash, String model, Double temperature, Integer topK) {
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

    private final ToolCallingManager toolCallingManager;
    private final ObservationRegistry observationRegistry;
    private final RetryTemplate retryTemplate;

    private final ConcurrentMap<CacheKey, ChatModel> cache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> CACHE_KEY_CACHE = new ConcurrentHashMap<>();

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
        this.toolCallingManager = toolCallingManagerProvider.getIfAvailable();
        this.observationRegistry = observationRegistryProvider.getIfAvailable();
        this.retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryUtils.DEFAULT_RETRY_TEMPLATE);
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
        Objects.requireNonNull(platform, "platform must not be null");

        if (StringUtils.isBlank(apiKey) && StringUtils.isBlank(url) && StringUtils.isBlank(model) && temperature == null && topK == null) {
            return getDefaultChatModel(platform);
        }

        ChatModel defaultModel = tryGetDefaultChatModel(platform);
        if (defaultModel != null && StringUtils.isBlank(apiKey) && StringUtils.isBlank(url) && StringUtils.isBlank(model) && temperature == null && topK == null) {
            return defaultModel;
        }

        String resolvedModel = resolveDefaultModel(platform, model);
        Double resolvedTemperature = temperature != null ? temperature : AiModelConstants.Model.DEFAULT_TEMPERATURE;
        Integer resolvedTopK = topK;

        ChatModel created = getOrCreateFromCache(platform, apiKey, url, resolvedModel, resolvedTemperature, resolvedTopK);
        return wrapTokenCountingIfNecessary(platform.getCode(), resolvedModel, created);
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
    public ChatModel getChatModel(String providerId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new IllegalArgumentException("providerId must not be blank");
        }

        BubbleAiProperties.ProviderConfig config = properties.getProviders().get(providerId);
        if (config == null) {
            throw new IllegalArgumentException("No provider config found for id: " + providerId);
        }

        return getOrCreateChatModel(
                config.getPlatform(),
                config.getApiKey(),
                config.getBaseUrl(),
                config.getModel(),
                config.getTemperature(),
                config.getTopK()
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
        };
    }

    private ChatModel getOrCreateFromCache(AiPlatformEnum platform, String apiKey, String url, String resolvedModel, Double resolvedTemperature, Integer resolvedTopK) {
        CacheKey key = new CacheKey(
                platform.getCode(),
                StringUtils.blankToDefault(url, ""),
                hashApiKey(apiKey),
                resolvedModel,
                resolvedTemperature,
                resolvedTopK
        );

        return cache.computeIfAbsent(key, ignored -> createChatModel(platform, apiKey, url, resolvedModel, resolvedTemperature, resolvedTopK));
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

    private ChatModel createChatModel(AiPlatformEnum platform, String apiKey, String url, String modelName, Double temperature, Integer topK) {
        log.info("Creating ChatModel for platform: {}, API Key: {}, Base URL: {}, Model: {}", 
                platform, 
                SensitiveDataUtils.maskApiKey(apiKey),
                SensitiveDataUtils.maskUrl(url),
                modelName);

        return switch (platform) {
            case OPENAI -> buildOpenAiChatModel(apiKey, url, modelName, temperature, null);
            case DEEPSEEK -> buildDeepSeekChatModel(apiKey, url, modelName, temperature, topK);
            case OLLAMA -> buildOllamaChatModel(url, modelName, temperature, topK);
            case ANTHROPIC -> buildAnthropicChatModel(apiKey, url, modelName, temperature, topK);
            case ZHIPU -> buildZhipuAiChatModel(apiKey, url, modelName, temperature, topK);
            case MINIMAX -> buildMinimaxChatModel(apiKey, url, modelName, temperature, topK);
            case SILICONFLOW -> buildSiliconFlowChatModel(apiKey, url, modelName, temperature, topK);
            case GEMINI -> {
                ChatModel injected = geminiChatModelProvider.getIfAvailable();
                if (injected != null) {
                    yield injected;
                }
                throw new IllegalStateException("GEMINI dynamic creation is not supported. Please configure a VertexAiGeminiChatModel bean.");
            }
        };
    }

    /**
     * 构建 OpenAI ChatModel
     *
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topP        Top P
     * @return {@link OpenAiChatModel}
     */
    private OpenAiChatModel buildOpenAiChatModel(String apiKey, String baseUrl, String model, Double temperature, Double topP) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        OpenAiApi.Builder apiBuilder = OpenAiApi.builder().apiKey(apiKey);
        if (StringUtils.isNotBlank(baseUrl)) {
            apiBuilder.baseUrl(baseUrl);
        }
        OpenAiApi api = apiBuilder.build();
        OpenAiChatOptions options = OpenAiChatOptions.builder().model(model).temperature(temperature).topP(topP).build();

        OpenAiChatModel.Builder builder = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .retryTemplate(retryTemplate);
        if (toolCallingManager != null) {
            builder.toolCallingManager(toolCallingManager);
        }
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder.build();
    }

    /**
     * 构建 Ollama ChatModel
     *
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return {@link OllamaChatModel}
     */
    private OllamaChatModel buildOllamaChatModel(String baseUrl, String model, Double temperature, Integer topK) {
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = AiModelConstants.Ollama.DEFAULT_BASE_URL;
        }
        OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();
        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        OllamaChatOptions options = optionsBuilder.build();

        OllamaChatModel.Builder builder = OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(options);
        if (toolCallingManager != null) {
            builder.toolCallingManager(toolCallingManager);
        }
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder.build();
    }

    /**
     * 构建 Anthropic ChatModel
     *
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return {@link AnthropicChatModel}
     */
    private AnthropicChatModel buildAnthropicChatModel(String apiKey, String baseUrl, String model, Double temperature, Integer topK) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "https://api.anthropic.com";
        }

        AnthropicApi api = AnthropicApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        AnthropicChatOptions options = optionsBuilder.build();

        AnthropicChatModel.Builder builder = AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(options)
                .retryTemplate(retryTemplate);
        if (toolCallingManager != null) {
            builder.toolCallingManager(toolCallingManager);
        }
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder.build();
    }

    /**
     * 构建 DeepSeek ChatModel
     *
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return {@link DeepSeekChatModel}
     */
    private DeepSeekChatModel buildDeepSeekChatModel(String apiKey, String baseUrl, String model, Double temperature, Integer topK) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "https://api.deepseek.com";
        }

        DeepSeekApi api = DeepSeekApi.builder().apiKey(apiKey).baseUrl(baseUrl).build();
        DeepSeekChatOptions.Builder optionsBuilder = DeepSeekChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        DeepSeekChatOptions options = optionsBuilder.build();

        DeepSeekChatModel.Builder builder = DeepSeekChatModel.builder()
                .deepSeekApi(api)
                .defaultOptions(options)
                .retryTemplate(retryTemplate);
        if (toolCallingManager != null) {
            builder.toolCallingManager(toolCallingManager);
        }
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder.build();
    }

    /**
     * 构建 ZhipuAI ChatModel
     *
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return {@link ZhiPuAiChatModel}
     */
    private ZhiPuAiChatModel buildZhipuAiChatModel(String apiKey, String baseUrl, String model, Double temperature, Integer topK) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        ZhiPuAiApi.Builder builder = ZhiPuAiApi.builder().apiKey(apiKey);
        if (StringUtils.isNotBlank(baseUrl)) {
            builder.baseUrl(baseUrl);
        } else {
            builder.baseUrl("https://open.bigmodel.cn/api/paas/");
        }
        ZhiPuAiApi api = builder.build();
        ZhiPuAiChatOptions.Builder optionsBuilder = ZhiPuAiChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        ZhiPuAiChatOptions options = optionsBuilder.build();

        return new ZhiPuAiChatModel(api, options, toolCallingManager, retryTemplate, observationRegistry);
    }

    /**
     * 构建 Minimax ChatModel
     *
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return {@link MiniMaxChatModel}
     */
    private MiniMaxChatModel buildMinimaxChatModel(String apiKey, String baseUrl, String model, Double temperature, Integer topK) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }

        MiniMaxApi api = StringUtils.isEmpty(baseUrl) ? new MiniMaxApi(apiKey) : new MiniMaxApi(baseUrl, apiKey);
        MiniMaxChatOptions.Builder optionsBuilder = MiniMaxChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        MiniMaxChatOptions options = optionsBuilder.build();

        return new MiniMaxChatModel(api, options, toolCallingManager, retryTemplate);
    }

    /**
     * 构建 SiliconFlow ChatModel
     *
     * SiliconFlow API 兼容 OpenAI 接口，使用 OpenAiChatModel 实现
     *
     * @param apiKey      API Key
     * @param baseUrl     Base URL
     * @param model       模型名称
     * @param temperature 温度
     * @param topK        Top K
     * @return {@link OpenAiChatModel}
     */
    private OpenAiChatModel buildSiliconFlowChatModel(String apiKey, String baseUrl, String model, Double temperature, Integer topK) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "https://api.siliconflow.cn";
        }

        OpenAiApi.Builder apiBuilder = OpenAiApi.builder().apiKey(apiKey).baseUrl(baseUrl);
        OpenAiApi api = apiBuilder.build();
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder().model(model).temperature(temperature);
        applyTopKIfSupported(optionsBuilder, topK);
        OpenAiChatOptions options = optionsBuilder.build();

        OpenAiChatModel.Builder builder = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .retryTemplate(retryTemplate);
        if (toolCallingManager != null) {
            builder.toolCallingManager(toolCallingManager);
        }
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder.build();
    }

    private static void applyTopKIfSupported(Object builder, Integer topK) {
        if (builder == null || topK == null) {
            return;
        }
        applyMethodIfPresent(builder, "topK", Integer.class, topK);
        applyMethodIfPresent(builder, "topK", int.class, topK);
        applyMethodIfPresent(builder, "top_k", Integer.class, topK);
        applyMethodIfPresent(builder, "top_k", int.class, topK);
    }

    private static void applyMethodIfPresent(Object target, String methodName, Class<?> parameterType, Object value) {
        try {
            target.getClass().getMethod(methodName, parameterType).invoke(target, value);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException("Failed to apply option: " + methodName, e);
        }
    }
}
