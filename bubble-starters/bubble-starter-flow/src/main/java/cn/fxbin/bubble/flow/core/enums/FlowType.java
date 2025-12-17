package cn.fxbin.bubble.flow.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作流类型
 *
 * @author FanXJ
 * @since 2025-08-05 16:52
 */
@Getter
@AllArgsConstructor
public enum FlowType {
    /**
     * 探索平台
     */
    EXPLORE(0),
    /**
     * 训练平台
     */
    TRAIN(1);

    /**
     * 节点类型编码
     */
    @JsonValue
    @EnumValue
    private final int code;
}
