package cn.fxbin.bubble.fireworks.data.mybatis.util;

import cn.fxbin.bubble.fireworks.core.model.PageRequest;
import cn.fxbin.bubble.fireworks.core.model.PageResult;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * PageUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/4/5 21:55
 */
public class PageUtils extends PageUtil {

    /**
     * 构建{@link Page}MyBatis Plus分页对象
     *
     * @param request 请求
     * @return {@link Page}<{@link T}>
     */
    public <T> Page<T> buildPageObj(PageRequest request) {
        return new Page<T>(request.getPageNo(), request.getPageSize());
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
     * @param page    页面
     * @return {@link PageResult}<{@link T}>
     */
    public <T> PageResult<T> buildPageResult(PageRequest request, Page<T> page) {
        return new PageResult<T>()
                .compute(request.getPageSize(), request.getPageNo(), page.getTotal())
                .setList(page.getRecords());
    }

}
