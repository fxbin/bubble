package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 分页查询请求模型
 * 
 * <p>用于支持分页查询的统一请求参数结构。</p>
 * 
 * <h3>请求示例：</h3>
 * <pre>{@code
 * {
 *   "page": 1,
 *   "page_size": 50,
 *   "sort_direction": "desc",
 *   "sort_field": "updated_at",
 *   "status_filter": "processed"
 * }
 * }</pre>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageRequest  implements Serializable {

    private Integer page;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("sort_direction")
    private String sortDirection;

    @JsonProperty("sort_field")
    private String sortField;

    @JsonProperty("status_filter")
    private String statusFilter;

}