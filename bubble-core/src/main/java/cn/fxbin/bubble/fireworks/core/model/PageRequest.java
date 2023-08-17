package cn.fxbin.bubble.fireworks.core.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
@ToString
@ApiModel("分页参数")
public class PageRequest implements Serializable {

    @ApiModelProperty(value = "页码，从 1 开始", required = true, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数，最大值为 100", required = true, example = "10")
    @NotNull(message = "每页条数不能为空")
    @Range(min = 1, max = 100, message = "条数范围为 [1, 100]")
    private Integer pageSize = 10;

    @Getter
    @Setter
    @ApiModelProperty(value = "排序规则")
    private List<Sort> sorts = new ArrayList<>();

    @Getter
    @Setter
    @ApiModel("排序元素")
    public static class Sort {

        /**
         * 排序字段
         */
        @ApiModelProperty(value = "排序字段")
        private String field;

        /**
         * 是否正序排序
         */
        @ApiModelProperty(value = "是否正序排序")
        private boolean asc;

    }

    public Integer getPageNo() {
        return pageNo;
    }

    public PageRequest setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public PageRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @ApiModelProperty(hidden = true)
    public final int getOffset() {
        return (pageNo - 1) * pageSize;
    }


}
