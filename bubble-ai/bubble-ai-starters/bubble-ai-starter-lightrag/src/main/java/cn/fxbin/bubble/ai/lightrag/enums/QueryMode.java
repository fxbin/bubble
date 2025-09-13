package cn.fxbin.bubble.ai.lightrag.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * QueryMode
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 10:18
 */
@Getter
@RequiredArgsConstructor
public enum QueryMode {

    LOCAL("local"),

    GLOBAL("global"),

    HYBRID("hybrid"),

    NAIVE("naive"),

    MIX("mix"),

    BYPASS("bypass");

    @JsonValue
    private final String value;

    public static QueryMode fromValue(String value) {
        for (QueryMode mode : QueryMode.values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

}
