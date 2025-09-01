package cn.fxbin.bubble.ai.lightrag.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DocumentClearCacheRequest
 *
 * <p>
 * 清除缓存请求模型
 * <p>
 * 示例数据：
 * <pre>{@code
 * {
 *   "modes": [
 *     "default",
 *     "naive"
 *   ]
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentClearCacheRequest implements Serializable {

    private List<String> modes;

}
