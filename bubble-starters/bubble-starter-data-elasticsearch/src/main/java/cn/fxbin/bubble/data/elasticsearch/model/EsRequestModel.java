package cn.fxbin.bubble.data.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * EsRequestModel
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/26 11:45
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsRequestModel implements Serializable {
    private static final long serialVersionUID = 5553543876030442241L;

    /**
     * elasticsearch index name
     */
    @NonNull private String indexName;

    /**
     * elasticsearch alias
     */
    private String alias;

    /**
     * elasticsearch doc source list
     */
    private List<DocSource> dataList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocSource {

        /**
         * elasticsearch doc id
         */
        @NonNull private String id;

        /**
         * elasticsearch doc source
         */
        private Object source;
    }


}
