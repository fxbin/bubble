package cn.fxbin.bubble.ai.domain.dto;

/**
 * AiModelTestResult
 * AI模型测试结果
 * 用于表示模型连接测试的返回结果，包含模型可用性、响应延迟等信息
 *
 * @param available  模型是否可用，true表示模型连接正常，false表示连接失败
 * @param message    测试结果消息，包含成功或失败的详细描述信息
 * @param modelName  模型名称，如 "gpt-4"、"claude-3-opus" 等
 * @param platform   模型所属平台，如 "openai"、"anthropic"、"azure" 等
 * @param latencyMs  响应延迟时间（毫秒），表示从发送请求到收到响应的时间
 * @param id         模型ID或测试记录ID，用于标识具体的模型或测试记录
 *
 * @author fxbin
 * @version v1.0
 * @since 2026-02-12 06:29:02
 */
public record AiModelTestResult(
        boolean available,
        String message,
        String modelName,
        String platform,
        Long latencyMs,
        Object id
) {
}
