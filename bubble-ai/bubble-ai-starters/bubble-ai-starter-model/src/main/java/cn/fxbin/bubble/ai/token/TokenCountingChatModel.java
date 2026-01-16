package cn.fxbin.bubble.ai.token;

import cn.fxbin.bubble.ai.constants.AiModelConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import reactor.core.publisher.Flux;

/**
 * Token 计数 ChatModel 装饰器
 * <p>装饰器模式，用于统计和记录Token使用情况</p>
 *
 * @author fxbin
 */
@Slf4j
public class TokenCountingChatModel implements ChatModel {

    private final ChatModel delegate;
    private final TokenUsageRecorder recorder;
    private final TokenCountEstimator tokenCountEstimator;
    private final String platform;
    private final String modelName;
    private final boolean streamEstimationEnabled;
    private final int maxBufferSize;

    public TokenCountingChatModel(ChatModel delegate, TokenUsageRecorder recorder, TokenCountEstimator tokenCountEstimator, String platform, String modelName) {
        this(delegate, recorder, tokenCountEstimator, platform, modelName, false);
    }

    public TokenCountingChatModel(ChatModel delegate, TokenUsageRecorder recorder, TokenCountEstimator tokenCountEstimator, String platform, String modelName, boolean streamEstimationEnabled) {
        this(delegate, recorder, tokenCountEstimator, platform, modelName, streamEstimationEnabled, AiModelConstants.Streaming.MAX_STREAM_BUFFER_SIZE);
    }

    public TokenCountingChatModel(ChatModel delegate, TokenUsageRecorder recorder, TokenCountEstimator tokenCountEstimator, String platform, String modelName, boolean streamEstimationEnabled, int maxBufferSize) {
        this.delegate = delegate;
        this.recorder = recorder;
        this.tokenCountEstimator = tokenCountEstimator;
        this.platform = platform;
        this.modelName = modelName;
        this.streamEstimationEnabled = streamEstimationEnabled;
        this.maxBufferSize = maxBufferSize;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        ChatResponse response = delegate.call(prompt);
        recordUsage(prompt, response);
        return response;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.defer(() -> {
            StringBuilder sampledContent = new StringBuilder();
            Usage[] finalUsage = new Usage[1];
            int[] totalLength = new int[1];

            return delegate.stream(prompt)
                    .doOnNext(response -> {
                        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                            Usage u = response.getMetadata().getUsage();
                            if (isValidUsage(u)) {
                                finalUsage[0] = u;
                            }
                        }

                        if (streamEstimationEnabled && finalUsage[0] == null) {
                            if (response.getResult() != null && response.getResult().getOutput() != null) {
                                String text = response.getResult().getOutput().getText();
                                if (text != null) {
                                    if (totalLength[0] < maxBufferSize) {
                                        int remaining = maxBufferSize - totalLength[0];
                                        sampledContent.append(text.substring(0, Math.min(text.length(), remaining)));
                                        totalLength[0] += text.length();
                                    }
                                }
                            }
                        }
                    })
                    .doOnComplete(() -> {
                        if (finalUsage[0] != null) {
                            recordUsage(new TokenUsageContext(platform, modelName, finalUsage[0]));
                        } else if (streamEstimationEnabled && tokenCountEstimator != null && sampledContent.length() >= AiModelConstants.Streaming.MIN_SAMPLE_SIZE) {
                            String estimatedContent = sampledContent.toString();
                            if (totalLength[0] >= maxBufferSize) {
                                log.debug("Stream response exceeded buffer size ({} chars), using sampled content ({} chars) for estimation. Platform: {}, Model: {}", 
                                         totalLength[0], estimatedContent.length(), platform, modelName);
                            }
                            Usage estimated = estimateUsage(prompt, estimatedContent);
                            if (estimated != null) {
                                recordUsage(new TokenUsageContext(platform, modelName, estimated));
                            }
                        }
                    });
        });
    }

    private void recordUsage(Prompt prompt, ChatResponse response) {
        Usage usage = null;
        if (response.getMetadata() != null) {
            usage = response.getMetadata().getUsage();
        }

        if (!isValidUsage(usage) && tokenCountEstimator != null) {
            String content = (response != null && response.getResult() != null && response.getResult().getOutput() != null)
                    ? response.getResult().getOutput().getText()
                    : "";
            usage = estimateUsage(prompt, content);
        }

        if (usage != null) {
            recordUsage(new TokenUsageContext(platform, modelName, usage));
        }
    }

    private Usage estimateUsage(Prompt prompt, String responseContent) {
        try {
            int promptTokens = tokenCountEstimator.estimate(buildPromptText(prompt));
            int completionTokens = tokenCountEstimator.estimate(responseContent);

            return new SimpleUsage(promptTokens, completionTokens);
        } catch (Exception e) {
            log.warn("Failed to estimate token usage", e);
            return null;
        }
    }

    private String buildPromptText(Prompt prompt) {
        if (prompt == null || prompt.getInstructions() == null) {
            return "";
        }

        return prompt.getInstructions().stream()
                .map(this::getMessageText)
                .filter(text -> text != null && !text.isBlank())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String getMessageText(Message message) {
        if (message == null) {
            return "";
        }
        try {
            return message.getText();
        } catch (Exception e) {
            return String.valueOf(message);
        }
    }

    private void recordUsage(TokenUsageContext context) {
        // Use Virtual Thread for async recording to avoid blocking the main thread
        Thread.ofVirtual().start(() -> {
            try {
                recorder.record(context);
            } catch (Exception e) {
                log.error("Failed to record token usage", e);
            }
        });
    }
    
    private boolean isValidUsage(Usage usage) {
        // Check if usage has non-zero values if possible, or just not null
        // Some providers return empty usage object with nulls or zeros.
        return usage != null && (usage.getTotalTokens() != null && usage.getTotalTokens() > 0);
    }
    
    // Simple implementation of Usage interface
    record SimpleUsage(Integer promptTokens, Integer completionTokens) implements Usage {

        @Override
        public Object getNativeUsage() {
            return null;
        }

        @Override
        public Integer getPromptTokens() {
            return promptTokens;
        }

        public Integer getGenerationTokens() {
            return completionTokens;
        }

        @Override
        public Integer getCompletionTokens() {
            return completionTokens;
        }

        @Override
        public Integer getTotalTokens() {
            return promptTokens + completionTokens;
        }
    }
}
