package cn.fxbin.bubble.ai.token;

import org.springframework.ai.chat.metadata.Usage;

/**
 * Token Usage Context
 *
 * @param platform Platform name (e.g., openai, ollama)
 * @param model    Model name (e.g., gpt-4, llama2)
 * @param usage    Token usage statistics
 * @author fxbin
 */
public record TokenUsageContext(String platform, String model, Usage usage) {
}
