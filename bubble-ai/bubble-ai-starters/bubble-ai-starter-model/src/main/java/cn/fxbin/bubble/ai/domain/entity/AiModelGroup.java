package cn.fxbin.bubble.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI model group metadata.
 *
 * @author fxbin
 */
@Data
@TableName("ai_model_group")
public class AiModelGroup implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Unique group code used for runtime routing.
     */
    private String groupCode;

    /**
     * Display name for management UI.
     */
    private String groupName;

    /**
     * Group description.
     */
    private String description;

    /**
     * Model type, e.g. CHAT / EMBEDDING.
     */
    private String modelType;

    /**
     * Whether the group is enabled.
     */
    private Boolean enabled;

    /**
     * Whether failover is enabled for this group.
     */
    private Boolean failoverEnabled;

    /**
     * Cooldown window in seconds for failed candidates.
     */
    private Integer cooldownSeconds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
