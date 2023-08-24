package cn.fxbin.bubble.core.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * PageResult
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/9/21 15:12
 */
@Getter
@ApiModel("分页结果")
public class PageResult<T> implements Serializable {

    @ApiModelProperty(value = "数据", required = true)
    private List<T> list;

    @ApiModelProperty(value = "总量", required = true)
    private Long total;

    @ApiModelProperty(value = "总页数", required = true)
    private Long  totalPage;

    @ApiModelProperty(value = "页码，从 1 开始", required = true)
    private Long pageNo;

    @ApiModelProperty(value = "每页条数", required = true)
    private Long pageSize;

    public PageResult<T> setList(List<T> list) {
        this.list = list;
        return this;
    }

    public PageResult<T> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public PageResult<T> setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
        return this;
    }

    public PageResult<T> setPageNo(Long pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public PageResult<T> setPageSize(Long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public PageResult<T> compute(Integer pageSize, Integer pageNo, Long total) {
        return this.compute(pageSize.longValue(), pageNo.longValue(), total);
    }

    public PageResult<T> compute(Long pageSize, Long pageNo, Long total) {
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        if (total > 0) {
            this.total = total;

            if (total % this.pageSize > 0) {
                this.totalPage = (total / this.pageSize) + 1;
            } else {
                this.totalPage = (total / this.pageSize);
            }
        } else {
            this.totalPage = 0L;
            this.total = 0L;
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <R> PageResult<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> collect = this.getList().stream().map(mapper).collect(toList());
        return ((PageResult<R>)this).setList(collect);
    }

}
