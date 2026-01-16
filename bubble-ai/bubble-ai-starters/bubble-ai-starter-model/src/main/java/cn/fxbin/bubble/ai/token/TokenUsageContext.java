package cn.fxbin.bubble.ai.token;

import org.springframework.ai.chat.metadata.Usage;

/**
 * Token 使用上下文
 * <p>封装Token使用统计信息</p>
 *
 * @param platform 平台名称 (如 openai, ollama)
 * @param model    模型名称 (如 gpt-4, llama2)
 * @param usage    Token 使用统计
 * @author fxbin
 */
public record TokenUsageContext(String platform, String model, Usage usage) {
}
