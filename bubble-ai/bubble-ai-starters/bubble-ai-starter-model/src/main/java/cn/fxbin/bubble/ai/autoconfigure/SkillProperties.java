package cn.fxbin.bubble.ai.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Skill 模块配置项。
 * 对应前缀：bubble.ai.skill
 */
@Data
@ConfigurationProperties(prefix = "bubble.ai.skill")
public class SkillProperties {

    /**
     * 是否启用 skill 注入能力。
     */
    private boolean enabled = true;

    /**
     * 技能装载模式：file / db / hybrid。
     */
    private String mode = "hybrid";

    /**
     * hybrid 模式下是否优先使用数据库技能。 */
    private boolean dbFirst = true;

    /**
     * 文件技能根目录（为空时仅使用 classpath）。
     */
    private String fileBasePath = "";

    /**
     * 单次最多注入的技能数量。
     */
    private int maxInjectSkills = 3;

    /**
     * 单个技能内容注入的最大字符数。
     */
    private int maxSkillContentLength = 12000;

    /**
     * 用户未显式选择时，默认注入的技能 ID 列表。
     */
    private List<Long> defaultInjectSkillIds = List.of();
}
