package cn.fxbin.bubble.ai.lightrag.model.ollama;

import lombok.Data;

import java.io.Serializable;

/**
 * VersionInfo
 *
 * <p>示例数据：
 * <pre>
 * {
 *   "version": "0.1.10"
 * }
 * </pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 16:41
 */
@Data
public class VersionInfo implements Serializable {

    private String version;

}
