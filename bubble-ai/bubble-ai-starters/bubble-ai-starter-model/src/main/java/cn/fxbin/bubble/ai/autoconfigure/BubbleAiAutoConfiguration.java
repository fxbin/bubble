package cn.fxbin.bubble.ai.autoconfigure;

import cn.fxbin.bubble.ai.factory.AiModelFactory;
import cn.fxbin.bubble.ai.factory.AiModelFactoryImpl;
import cn.fxbin.bubble.ai.manager.AiModelManager;
import cn.fxbin.bubble.ai.manager.AiModelManagerImpl;
import cn.fxbin.bubble.ai.mapper.AiModelConfigMapper;
import cn.fxbin.bubble.ai.service.AiModelConfigService;
import cn.fxbin.bubble.ai.service.impl.AiModelConfigServiceImpl;
import cn.fxbin.bubble.ai.token.DefaultTokenUsageRecorder;
import cn.fxbin.bubble.ai.token.TokenUsageRecorder;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Bubble AI 自动配置
 *
 * @author fxbin
 */
@AutoConfiguration(afterName = "cn.fxbin.bubble.ai.autoconfigure.BubbleAiMybatisAutoConfiguration")
@EnableConfigurationProperties(BubbleAiProperties.class)
public class BubbleAiAutoConfiguration {

    /**
     * Token 使用记录器
     *
     * @return {@link TokenUsageRecorder}
     */
    @Bean
    @ConditionalOnMissingBean
    public TokenUsageRecorder tokenUsageRecorder() {
        return new DefaultTokenUsageRecorder();
    }

    /**
     * Token 数量估算器
     *
     * @return {@link TokenCountEstimator}
     */
    @Bean
    @ConditionalOnMissingBean
    public TokenCountEstimator tokenCountEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    /**
     * AI 模型工厂
     *
     * @param properties                 配置属性
     * @param tokenUsageRecorder         Token 使用记录器
     * @param tokenCountEstimator        Token 数量估算器
     * @param openAiChatModelProvider    OpenAI ChatModel Provider
     * @param ollamaChatModelProvider    Ollama ChatModel Provider
     * @param anthropicChatModelProvider Anthropic ChatModel Provider
     * @param geminiChatModelProvider    Gemini ChatModel Provider
     * @param deepSeekChatModelProvider  DeepSeek ChatModel Provider
     * @param zhipuAiChatModelProvider   ZhipuAI ChatModel Provider
     * @param minimaxChatModelProvider   Minimax ChatModel Provider
     * @param toolCallingManagerProvider ToolCallingManager Provider
     * @param observationRegistryProvider ObservationRegistry Provider
     * @param retryTemplateProvider      RetryTemplate Provider
     * @return {@link AiModelFactory}
     */
    @Bean
    @ConditionalOnMissingBean
    public AiModelFactory aiModelFactory(BubbleAiProperties properties, 
                                         TokenUsageRecorder tokenUsageRecorder,
                                         TokenCountEstimator tokenCountEstimator,
                                         ObjectProvider<OpenAiChatModel> openAiChatModelProvider,
                                         ObjectProvider<OllamaChatModel> ollamaChatModelProvider,
                                         ObjectProvider<AnthropicChatModel> anthropicChatModelProvider,
                                         ObjectProvider<VertexAiGeminiChatModel> geminiChatModelProvider,
                                         ObjectProvider<DeepSeekChatModel> deepSeekChatModelProvider,
                                         ObjectProvider<ZhiPuAiChatModel> zhipuAiChatModelProvider,
                                         ObjectProvider<MiniMaxChatModel> minimaxChatModelProvider,
                                         ObjectProvider<org.springframework.ai.model.tool.ToolCallingManager> toolCallingManagerProvider,
                                         ObjectProvider<io.micrometer.observation.ObservationRegistry> observationRegistryProvider,
                                         ObjectProvider<org.springframework.retry.support.RetryTemplate> retryTemplateProvider) {
        return new AiModelFactoryImpl(properties, tokenUsageRecorder, tokenCountEstimator,
                                      openAiChatModelProvider, 
                                      ollamaChatModelProvider, 
                                      anthropicChatModelProvider, 
                                      geminiChatModelProvider,
                                      deepSeekChatModelProvider,
                                      zhipuAiChatModelProvider,
                                      minimaxChatModelProvider,
                                      toolCallingManagerProvider,
                                      observationRegistryProvider,
                                      retryTemplateProvider);
    }
    
    /**
     * AI 模型管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public AiModelManager aiModelManager(BubbleAiProperties properties, 
                                         AiModelFactory aiModelFactory, 
                                         ObjectProvider<AiModelConfigService> aiModelConfigServiceProvider) {
        return new AiModelManagerImpl(properties, aiModelFactory, aiModelConfigServiceProvider);
    }
    
    /**
     * AI 模型配置服务
     *
     * @param aiModelFactory      AI 模型工厂
     * @param aiModelConfigMapper AI 模型配置 Mapper
     * @return {@link AiModelConfigService}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.baomidou.mybatisplus.extension.service.IService")
    @ConditionalOnBean(AiModelConfigMapper.class)
    public AiModelConfigService aiModelConfigService(AiModelFactory aiModelFactory, AiModelConfigMapper aiModelConfigMapper) {
        return new AiModelConfigServiceImpl(aiModelFactory, aiModelConfigMapper);
    }

}
