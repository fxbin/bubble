package cn.fxbin.bubble.flow.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ExecStatus
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:11
 */
@Getter
@AllArgsConstructor
public enum FlowExecStatus {

    PENDING("pending", "等待执行"),

    RUNNING("running", "正在执行"),

    SUCCESS("success", "执行成功"),

    FAILED("failed", "执行失败"),

    SKIPPED("skipped", "已跳过");

//    @JsonValue
    private final String value;

    private final String desc;

}
