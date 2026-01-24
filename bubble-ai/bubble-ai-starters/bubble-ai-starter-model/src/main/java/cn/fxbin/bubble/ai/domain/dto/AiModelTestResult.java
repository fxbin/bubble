package cn.fxbin.bubble.ai.domain.dto;

public record AiModelTestResult(
        boolean available,
        String message,
        String modelName,
        String platform,
        Long latencyMs,
        Object id
) {
}
