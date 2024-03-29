package cn.fxbin.bubble.data.mybatisplus.util;

import cn.fxbin.bubble.core.dataobject.PageRequest;
import cn.fxbin.bubble.core.dataobject.PageResult;
import cn.fxbin.bubble.core.util.CollectionUtils;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PageUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/4/5 21:55
 */
@UtilityClass
public class PageUtils extends PageUtil {

    /**
     * 构建{@link Page}MyBatis Plus分页对象
     *
     * @param request 请求
     * @return {@link IPage}<{@link T}>
     */
    public <T> IPage<T> buildPage(PageRequest request) {
        // 页码 + 每页数量
        Page<T> page = new Page<>(request.getPageNo(), request.getPageSize());

        // 排序字段拼接
        List<PageRequest.SortItem> sorts = request.getSorts();
        if (CollectionUtils.isNotEmpty(sorts)) {
            page.addOrder(sorts.stream()
                    .map(sortItem -> sortItem.isAsc() ?
                            OrderItem.asc(sortItem.getField()) : OrderItem.desc(sortItem.getField()))
                    .collect(Collectors.toList()));
        }
        return page;
    }

    /**
     * 构建页面结果
     *
     * @param request 请求
     * @param list    列表
     * @param total   总计
     * @return {@link PageResult}<{@link T}>
     */
    public <T> PageResult<T> buildPageResult(PageRequest request, List<T> list, Long total) {
        return new PageResult<T>()
                .compute(request.getPageSize(), request.getPageNo(), total)
                .setList(list);
    }

    /**
     * 构建页面结果
     *
     * @param request 请求
     * @param mpPage  页面
     * @return {@link PageResult}<{@link T}>
     */
    public <T> PageResult<T> buildPageResult(PageRequest request, IPage<T> mpPage) {
        return new PageResult<T>()
                .compute(request.getPageSize(), request.getPageNo(), mpPage.getTotal())
                .setList(mpPage.getRecords());
    }

}
