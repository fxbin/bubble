package cn.fxbin.bubble.ai.factory.creator;

import cn.fxbin.bubble.ai.autoconfigure.BubbleAiProperties;
import io.micrometer.observation.ObservationRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * AI 模型创建器抽象基类
 * <p>提供HTTP客户端创建、连接池配置等通用功能</p>
 *
 * @author fxbin
 */
public abstract class AbstractAiModelCreator implements AiModelCreator {

    protected final BubbleAiProperties properties;
    protected final ToolCallingManager toolCallingManager;
    protected final ObservationRegistry observationRegistry;
    protected final RetryTemplate retryTemplate;

    private static final String CONNECTION_PROVIDER_NAME = "ai-model-connection-provider";
    private static final int MAX_CONNECTIONS = 100;
    private static final int MAX_IDLE_TIME_SECONDS = 20;
    private static final int PENDING_ACQUIRE_TIMEOUT_SECONDS = 60;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

    protected AbstractAiModelCreator(BubbleAiProperties properties, ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry, RetryTemplate retryTemplate) {
        this.properties = properties;
        this.toolCallingManager = toolCallingManager;
        this.observationRegistry = observationRegistry;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatModel createChatModel(cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum platform, String apiKey, String baseUrl, String model, Double temperature, Integer topK, Double topP) {
        throw new UnsupportedOperationException("ChatModel creation not supported for platform: " + platform);
    }

    @Override
    public EmbeddingModel createEmbeddingModel(cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum platform, String apiKey, String baseUrl, String model, Integer dimensions) {
        throw new UnsupportedOperationException("EmbeddingModel creation not supported for platform: " + platform);
    }

    /**
     * Create configured HttpClient with connection pool (for async calls)
     *
     * @return {@link HttpClient}
     */
    protected HttpClient createHttpClient() {
        BubbleAiProperties.HttpTimeout timeout = properties.getHttpTimeout();
        
        ConnectionProvider connectionProvider = ConnectionProvider.builder(CONNECTION_PROVIDER_NAME)
                .maxConnections(MAX_CONNECTIONS)
                .maxIdleTime(Duration.ofSeconds(MAX_IDLE_TIME_SECONDS))
                .pendingAcquireTimeout(Duration.ofSeconds(PENDING_ACQUIRE_TIMEOUT_SECONDS))
                .build();
        
        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout.getConnectTimeout())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout.getReadTimeout() / 1000, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout.getWriteTimeout(), TimeUnit.SECONDS)))
                .responseTimeout(Duration.ofMinutes(timeout.getResponseTimeout()));
    }

    /**
     * Create configured WebClient.Builder (for async calls)
     *
     * @return {@link WebClient.Builder}
     */
    protected WebClient.Builder createWebClientBuilder() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()));
    }

    /**
     * Create configured RestClient.Builder with connection pool (for sync calls)
     *
     * @return {@link RestClient.Builder}
     */
    protected RestClient.Builder createRestClientBuilder() {
        BubbleAiProperties.HttpTimeout timeout = properties.getHttpTimeout();
        
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(timeout.getConnectTimeout()))
                .build();
        
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(MAX_CONNECTIONS)
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectionRequestTimeout(timeout.getConnectionRequestTimeout());
        requestFactory.setReadTimeout(timeout.getReadTimeout());

        return RestClient.builder()
                .requestFactory(requestFactory);
    }

    protected static void applyMethodIfPresent(Object target, String methodName, Class<?> parameterType, Object value) {
        try {
            target.getClass().getMethod(methodName, parameterType).invoke(target, value);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException("Failed to apply option: " + methodName, e);
        }
    }

    protected static void applyTopKIfSupported(Object builder, Integer topK) {
        if (builder == null || topK == null) {
            return;
        }
        applyMethodIfPresent(builder, "topK", Integer.class, topK);
        applyMethodIfPresent(builder, "topK", int.class, topK);
        applyMethodIfPresent(builder, "top_k", Integer.class, topK);
        applyMethodIfPresent(builder, "top_k", int.class, topK);
    }

    protected static void applyTopPIfSupported(Object builder, Double topP) {
        if (builder == null || topP == null) {
            return;
        }
        applyMethodIfPresent(builder, "topP", Double.class, topP);
        applyMethodIfPresent(builder, "topP", double.class, topP);
        applyMethodIfPresent(builder, "top_p", Double.class, topP);
        applyMethodIfPresent(builder, "top_p", double.class, topP);
    }
}
