package cn.fxbin.bubble.flow.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * ArchiveStrategy
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/9/8 10:17
 */
@Getter
@RequiredArgsConstructor
public enum ArchiveStrategy implements Serializable {

    /** 按时间归档 - 超过指定天数的版本 */
    BY_TIME(0, "按时间归档 - 超过指定天数的版本"),
    /** 按版本数量归档 - 保留最新N个版本 */
    BY_COUNT(1, "按版本数量归档 - 保留最新N个版本"),
    /** 按使用频率归档 - 归档低频使用的版本 */
    BY_USAGE(2, "按使用频率归档 - 归档低频使用的版本"),
    /** 手动指定归档 - 明确指定要归档的版本 */
    MANUAL(3, "手动指定归档 - 明确指定要归档的版本");

    @EnumValue
    @JsonValue
    private final int value;

    private final String description;

}
