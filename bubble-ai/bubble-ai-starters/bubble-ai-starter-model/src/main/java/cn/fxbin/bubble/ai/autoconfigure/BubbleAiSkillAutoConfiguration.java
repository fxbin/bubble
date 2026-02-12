package cn.fxbin.bubble.ai.autoconfigure;

import cn.fxbin.bubble.ai.skills.SkillRegistry;
import cn.fxbin.bubble.ai.skills.impl.FileSkillRegistry;
import cn.fxbin.bubble.ai.skills.impl.SkillPromptAssembler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Skill 基础设施自动配置。
 * 当业务侧未提供 FileSkillRegistry 时，默认装配 FileSkillRegistry。
 */
@AutoConfiguration
@EnableConfigurationProperties(SkillProperties.class)
public class BubbleAiSkillAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileSkillRegistry.class)
    public FileSkillRegistry fileSkillRegistry(SkillProperties skillProperties) {
        return new FileSkillRegistry(skillProperties);
    }

    @Bean
    @ConditionalOnMissingBean(SkillPromptAssembler.class)
    public SkillPromptAssembler skillPromptAssembler(SkillRegistry skillRegistry, SkillProperties skillProperties) {
        return new SkillPromptAssembler(skillRegistry, skillProperties);
    }
}
