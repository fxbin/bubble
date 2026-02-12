package cn.fxbin.bubble.ai.skills;

/**
 * 统一的技能元数据描述。
 *
 * @param id 技能唯一标识（优先用于选择与注入）
 * @param name 技能名称（仅用于展示，不保证唯一）
 * @param description 技能简要描述
 * @param source 技能来源（如 db / file / classpath）
 * @param skillPath 技能路径（文件或类路径下的定位信息）
 */
public record SkillDescriptor(Long id, String name, String description, String source, String skillPath) {
}
