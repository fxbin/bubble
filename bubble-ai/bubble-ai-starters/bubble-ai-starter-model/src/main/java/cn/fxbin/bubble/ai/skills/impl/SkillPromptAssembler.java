package cn.fxbin.bubble.ai.skills.impl;

import cn.fxbin.bubble.ai.skills.SkillDescriptor;
import cn.fxbin.bubble.ai.autoconfigure.SkillProperties;
import cn.fxbin.bubble.ai.skills.SkillRegistry;
import cn.fxbin.bubble.core.util.ObjectUtils;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Skill 系统提示词组装器。
 * 采用“渐进披露”策略：先给技能目录，再注入被选中的技能全文。
 */
@RequiredArgsConstructor
public class SkillPromptAssembler {

    private static final String LINE_BREAK = "\n";
    private static final String DOUBLE_LINE_BREAK = "\n\n";
    private static final String SKILL_PROMPT_HEADER = "## Skills System";
    private static final String SKILL_FULL_CONTENT_HEADER = "## Selected Skill Instructions";

    private final SkillRegistry skillRegistry;
    private final SkillProperties skillProperties;

    public String buildSystemPrompt(String promptContent, List<Long> selectedSkillIds) {
        if (!skillProperties.isEnabled()) {
            return promptContent;
        }

        List<SkillDescriptor> availableSkills = resolveAvailableSkillDescriptors();
        List<Long> effectiveSkillIds = resolveEffectiveSkillIds(selectedSkillIds);
        List<SkillContent> selectedSkills = resolveSelectedSkillContentsById(effectiveSkillIds);
        String catalogSection = buildSkillCatalogSection(availableSkills);
        String selectedSection = buildSelectedSkillSection(selectedSkills);
        String skillSection = Stream.of(catalogSection, selectedSection)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining(DOUBLE_LINE_BREAK));
        if (StrUtil.isBlank(skillSection)) {
            return promptContent;
        }
        if (StrUtil.isBlank(promptContent)) {
            return skillSection;
        }
        return promptContent + DOUBLE_LINE_BREAK + skillSection;
    }

    private List<SkillDescriptor> resolveAvailableSkillDescriptors() {
        List<SkillDescriptor> fromRegistry = skillRegistry.listAll();
        if (ObjectUtils.isEmpty(fromRegistry)) {
            return List.of();
        }
        return fromRegistry.stream()
                .sorted(Comparator.comparing(SkillDescriptor::id, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(SkillDescriptor::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private List<Long> resolveEffectiveSkillIds(List<Long> selectedSkillIds) {
        // 用户显式选择优先；未选择时使用默认注入配置。
        if (ObjectUtils.isNotEmpty(selectedSkillIds)) {
            return selectedSkillIds;
        }
        return skillProperties.getDefaultInjectSkillIds();
    }

    private List<SkillContent> resolveSelectedSkillContentsById(List<Long> selectedSkillIds) {
        if (ObjectUtils.isEmpty(selectedSkillIds)) {
            return List.of();
        }
        int maxInjectSkills = Math.max(0, skillProperties.getMaxInjectSkills());
        return selectedSkillIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(maxInjectSkills)
                .map(this::readSkillById)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<SkillContent> readSkillById(Long skillId) {
        String skillName = skillRegistry.findSkillById(skillId)
                .map(SkillDescriptor::name)
                .orElse("skill-" + skillId);
        return skillRegistry.readContentById(skillId)
                .filter(StrUtil::isNotBlank)
                .map(this::truncateSkillContent)
                .map(content -> new SkillContent(skillName, content, skillId));
    }

    private String truncateSkillContent(String content) {
        int maxLen = Math.max(0, skillProperties.getMaxSkillContentLength());
        if (maxLen == 0 || content.length() <= maxLen) {
            return content;
        }
        return content.substring(0, maxLen) + "\n\n...[truncated]";
    }

    private String buildSkillCatalogSection(List<SkillDescriptor> skills) {
        if (ObjectUtils.isEmpty(skills)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(SKILL_PROMPT_HEADER).append(DOUBLE_LINE_BREAK);
        builder.append("Available skills (progressive disclosure):").append(LINE_BREAK);
        for (SkillDescriptor skill : skills) {
            String idText = skill.id() == null ? "-" : String.valueOf(skill.id());
            builder.append("- [id=").append(idText).append("] ")
                    .append("**").append(skill.name()).append("**");
            if (StrUtil.isNotBlank(skill.description())) {
                builder.append(": ").append(skill.description());
            }
            builder.append(" (source=").append(StrUtil.blankToDefault(skill.source(), "unknown"));
            if (StrUtil.isNotBlank(skill.skillPath())) {
                builder.append(", path=").append(skill.skillPath());
            }
            builder.append(")").append(LINE_BREAK);
        }
        if (StrUtil.isNotBlank(skillRegistry.getSkillLoadInstructions())) {
            builder.append(LINE_BREAK)
                    .append("Load instructions: ")
                    .append(skillRegistry.getSkillLoadInstructions())
                    .append(LINE_BREAK);
        }
        return builder.toString().trim();
    }

    private String buildSelectedSkillSection(List<SkillContent> skills) {
        if (ObjectUtils.isEmpty(skills)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(SKILL_FULL_CONTENT_HEADER).append(DOUBLE_LINE_BREAK);
        for (SkillContent skill : skills) {
            builder.append("### [id=").append(skill.id()).append("] ").append(skill.name()).append(LINE_BREAK)
                    .append(skill.content().trim())
                    .append(DOUBLE_LINE_BREAK);
        }
        return builder.toString().trim();
    }

    private record SkillContent(String name, String content, Long id) {
    }
}
