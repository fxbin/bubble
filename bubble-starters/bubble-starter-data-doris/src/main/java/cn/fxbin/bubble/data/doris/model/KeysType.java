package cn.fxbin.bubble.data.doris.model;

/**
 * KeysType
 * Doris 表支持的 Key 类型
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
public enum KeysType {

    /**
     * DUPLICATE KEY
     */
    DUPLICATE,

    /**
     * UNIQUE KEY
     */
    UNIQUE,

    /**
     * AGGREGATE KEY
     */
    AGGREGATE,

    /**
     * PRIMARY KEY (Doris 1.2.x 及更高版本支持)
     */
    PRIMARY

}