package cn.fxbin.bubble.flow.core.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * BizEntity
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/15 16:37
 */
@Data
public class BizEntity implements Serializable {

    /**
     * 环境ID
     */
    private String envId;

    /**
     * 租户ID
     */
    private String tenantId;


}
