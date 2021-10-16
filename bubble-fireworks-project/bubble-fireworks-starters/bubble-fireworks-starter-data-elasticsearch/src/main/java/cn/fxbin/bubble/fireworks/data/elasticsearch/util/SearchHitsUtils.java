package cn.fxbin.bubble.fireworks.data.elasticsearch.util;

import lombok.experimental.UtilityClass;
import org.elasticsearch.search.SearchHits;

/**
 * SearchHitsUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/26 16:42
 */
@UtilityClass
public class SearchHitsUtils {

    public long getTotalCount(SearchHits searchHits) {
        return searchHits.getTotalHits().value;
    }

}
