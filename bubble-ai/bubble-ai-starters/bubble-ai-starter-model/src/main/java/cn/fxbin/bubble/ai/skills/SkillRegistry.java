package cn.fxbin.bubble.ai.skills;

import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Optional;

/**
 * 技能注册中心抽象。
 * 负责统一提供技能元数据查询与技能内容读取能力。
 */
public interface SkillRegistry {

    List<SkillDescriptor> listAvailableSkills();

    default List<SkillDescriptor> listAll() {
        return listAvailableSkills();
    }

    Optional<String> readSkillContent(String skillName);

    default Optional<SkillDescriptor> getByName(String skillName) {
        if (StrUtil.isBlank(skillName)) {
            return Optional.empty();
        }
        return listAvailableSkills().stream()
                .filter(skill -> skill != null && skillName.equals(skill.name()))
                .findFirst();
    }

    default Optional<String> readContent(String skillName) {
        return readSkillContent(skillName);
    }

    Optional<SkillDescriptor> findSkillById(Long skillId);

    Optional<String> readSkillContentById(Long skillId);

    default Optional<String> readContentById(Long skillId) {
        return readSkillContentById(skillId);
    }

    default void reload() {
    }

    default String getRegistryType() {
        return "unknown";
    }

    default String getSkillLoadInstructions() {
        return "";
    }
}
