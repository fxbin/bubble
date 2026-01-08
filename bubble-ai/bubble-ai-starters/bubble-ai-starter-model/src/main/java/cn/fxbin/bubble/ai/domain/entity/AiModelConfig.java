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
     */
    private Double temperature;

    private Integer topK;

    /**
     * 是否启用
     */
    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
