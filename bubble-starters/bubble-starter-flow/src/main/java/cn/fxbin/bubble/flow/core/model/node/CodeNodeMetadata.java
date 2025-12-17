package cn.fxbin.bubble.flow.core.model.node;

import lombok.Data;

import java.io.Serializable;

/**
 * CodeNodeMetadata
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:57
 */
@Data
public class CodeNodeMetadata implements Serializable {

    /**
     * 语言
     */
    private String language;

    /**
     * 代码
     */
    private String code;

}
