package cn.fxbin.bubble.ai.lightrag.model.graph;

import cn.fxbin.bubble.ai.lightrag.model.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * GraphExistsResponse
 *
 * <pre>{@code
 * {
 *   "exists": false
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 10:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GraphExistsResponse extends BaseResponse implements Serializable {

    private boolean exists;

}
