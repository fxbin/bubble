package cn.fxbin.bubble.data.doris.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ColumnDefinition
 * Doris 列定义
 *
 * @author fxbin
 * @version v1.0
 * @since 2024/5/26 0:00
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDefinition {

    /**
     * 列名
     */
    private String name;

    /**
     * 列类型，例如: INT, BIGINT, VARCHAR(20), DECIMAL(9, 3)
     */
    private String type;

    /**
     * 是否允许为空
     */
    private boolean nullable;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 列注释
     */
    private String comment;

    /**
     * 是否是 Key 列 (DUPLICATE KEY, UNIQUE KEY, AGGREGATE KEY, PRIMARY KEY 的组成部分)
     * 对于 AGGREGATE KEY 模型，标记为 true 的列将作为 AGGREGATE KEY
     */
    private boolean isKey;

    /**
     * 聚合函数类型 (仅对 AGGREGATE KEY 模型有效)
     * 例如: SUM, MIN, MAX, REPLACE, HLL_UNION, BITMAP_UNION, COUNT
     */
    private String aggregationType;


    public ColumnDefinition(String name, String type, boolean nullable, String comment) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.comment = comment;
    }


}