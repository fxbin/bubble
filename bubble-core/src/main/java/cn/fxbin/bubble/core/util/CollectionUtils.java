package cn.fxbin.bubble.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;

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

}
