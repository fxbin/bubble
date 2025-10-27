package cn.fxbin.bubble.core.util;

import cn.hutool.core.collection.CollUtil;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CollectionUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 17:32
 */
@UtilityClass
public class CollectionUtils extends org.springframework.util.CollectionUtils {

    /**
     * isNotEmpty
     *
     * @since 2020/4/9 18:56
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     * @see org.springframework.util.CollectionUtils#isEmpty(java.util.Collection)
     */
    public boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * isNotEmpty
     *
     * @since 2020/4/9 18:56
     * @param map the Map to check
     * @return whether the given Map is empty
     * @see org.springframework.util.CollectionUtils#isEmpty(java.util.Map)
     */
    public boolean isNotEmpty(@Nullable Map<?, ?> map) {
        return !isEmpty(map);
    }


    /**
     * 将集合中的元素转换为另一种类型的列表
     *
     * 该方法接受一个源集合和一个转换函数，将集合中的每个元素应用转换函数，
     * 并过滤掉转换结果为null的元素，最终返回转换后的列表
     *
     * @param from 源集合，包含需要转换的元素
     * @param func 转换函数，将类型T的元素转换为类型U的元素
     * @param <T> 源集合中元素的类型
     * @param <U> 目标列表中元素的类型
     * @return 转换后的列表，如果源集合为空则返回空列表
     */
    public static <T, U> List<U> convertList(Collection<T> from, Function<T, U> func) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().map(func).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
