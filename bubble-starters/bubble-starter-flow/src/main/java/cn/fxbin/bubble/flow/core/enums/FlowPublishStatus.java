package cn.fxbin.bubble.flow.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FlowStatus
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/22 10:34
 */
@Getter
@AllArgsConstructor
public enum FlowPublishStatus {

    /**
     * 草稿
     */
    DRAFT(1),

    /**
     * 已发布
     */
    PUBLISHED(2)
    ;

    @JsonValue
    @EnumValue
    private final Integer status;
}
