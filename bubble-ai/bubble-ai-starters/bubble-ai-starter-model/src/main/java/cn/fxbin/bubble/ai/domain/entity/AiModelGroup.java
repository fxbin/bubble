package cn.fxbin.bubble.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 模型分组实体
 * <p>用于存储模型分组的展示信息与运行时策略，例如分组编码、名称、描述、故障切换开关和冷却时间</p>
 *
 * @author fxbin
 * @since 2026/03/19 16:47
 */
@Data
@TableName("ai_model_group")
public class AiModelGroup implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分组编码
     * <p>作为运行时路由与模型配置关联的唯一标识</p>
     */
    private String groupCode;

    /**
     * 分组名称
     * <p>主要用于界面管理与分组展示</p>
     */
    private String groupName;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 模型类型
     * <p>例如 CHAT / EMBEDDING，用于区分不同能力类型的分组</p>
     */
    private String modelType;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否启用组级故障切换
     */
    private Boolean failoverEnabled;

    /**
     * 故障冷却时间，单位秒
     */
    private Integer cooldownSeconds;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
