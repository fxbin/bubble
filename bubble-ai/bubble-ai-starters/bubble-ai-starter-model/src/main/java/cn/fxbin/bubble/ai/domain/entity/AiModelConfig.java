package cn.fxbin.bubble.ai.domain.entity;

import cn.fxbin.bubble.ai.domain.enums.AiPlatformEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 模型配置实体
 * <p>用于存储和管理AI模型的配置信息，包括平台、API密钥、模型参数等</p>
 *
 * @author fxbin
 * @since 2024/05/28
 */
@Data
@TableName("ai_model_config")
public class AiModelConfig implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置名称 (唯一标识，用于代码引用)
     */
    private String configName;

    /**
     * 平台 (openai, deepseek, siliconflow, ollama)
     */
    private AiPlatformEnum platform;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * Base URL
     */
    private String baseUrl;

    /**
     * 模型名称 (如 gpt-4, deepseek-chat)
     */
    private String model;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 温度
     * <p>控制随机性，数值越高输出越发散，数值越低输出越稳定</p>
     * <p>常见范围：0.0 - 2.0，建议在 0.2 - 0.9 之间调优</p>
     * <p>适用场景：创作发散可提高，事实问答与严谨写作可降低</p>
     * <p>与 TopK/TopP 组合使用时，温度优先影响整体随机性</p>
     */
    private Double temperature;

    /**
     * Top K
     * <p>限制候选词数量，仅在概率最高的 K 个词中采样</p>
     * <p>常见范围：1 - 200，K 越小越保守，越大越多样</p>
     * <p>适用场景：需要更强可控性与稳定性时使用较小 K</p>
     * <p>与 TopP 二选一或联合使用，建议优先以 TopP 为主</p>
     */
    private Integer topK;

    /**
     * Top P
     * <p>核采样阈值，保留累计概率不超过 P 的候选词集合</p>
     * <p>常见范围：0.0 - 1.0，P 越小越集中，越大越多样</p>
     * <p>适用场景：在保证多样性的同时控制输出质量</p>
     * <p>与 Temperature 组合时，建议先确定 TopP，再微调温度</p>
     */
    private Double topP;

    /**
     * 所属模型组编码
     */
    private String groupId;

    /**
     * 选中当前模型时，是否允许自动切换到同组其他模型
     */
    private Boolean fallbackToGroupEnabled;

    /**
     * 同组内优先级，值越小优先级越高
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
