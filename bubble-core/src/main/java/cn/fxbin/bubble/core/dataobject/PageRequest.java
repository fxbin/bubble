package cn.fxbin.bubble.core.dataobject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * PageRequest
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/9/21 14:51
 */
@Getter
@Setter
@ToString
@Schema(description = "分页参数")
public class PageRequest implements Serializable {

    @Schema(description = "页码，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数，最大值为 100", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "每页条数不能为空")
    @Range(min = 1, max = 100, message = "条数范围为 [1, 100]")
    private Integer pageSize = 10;

    @Setter
    @Schema(description = "排序规则")
    private List<SortItem> sorts = new ArrayList<>();

    @Getter
    @Setter
    @Schema(description = "排序元素")
    public static class SortItem {

        /**
         * 排序字段
         */
        @Schema(description = "排序字段")
        private String field;

        /**
         * 是否正序排序
         */
        @Schema(description = "是否正序排序", defaultValue = "true")
        private boolean asc = true;

    }

    @Schema(hidden = true)
    public final int getOffset() {
        return (pageNo - 1) * pageSize;
    }


}
