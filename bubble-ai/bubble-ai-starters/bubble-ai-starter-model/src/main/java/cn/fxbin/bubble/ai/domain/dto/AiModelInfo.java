package cn.fxbin.bubble.ai.domain.dto;

/**
 * AI 模型信息
 *
 * @param id          模型ID (唯一标识)
 * @param name        模型名称 (显示用)
 * @param code        模型代码 (如 gpt-4)
 * @param description 模型描述
 * @param platform    平台
 * @param source      来源 (CONFIG/DATABASE)
 * @author fxbin
 */
public record AiModelInfo(
        String id,
        String name,
        String code,
        String description,
        String platform,
        String source
) {
}
