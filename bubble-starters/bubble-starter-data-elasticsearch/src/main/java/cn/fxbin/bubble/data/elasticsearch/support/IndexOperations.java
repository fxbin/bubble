package cn.fxbin.bubble.data.elasticsearch.support;

import cn.fxbin.bubble.data.elasticsearch.exception.ElasticsearchException;
import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

/**
 * IndexOperations
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/26 14:35
 */
@Slf4j
public class IndexOperations extends AbstractElasticsearchSupport {

    public IndexOperations(RestHighLevelClient restHighLevelClient, Integer numberOfShards, Integer numberOfReplicas) {
        super(restHighLevelClient, numberOfShards, numberOfReplicas);
    }

    /**
     * index
     *
     * @since 2020/5/26 15:18
     * @param indexName index name
     */
    public void index(String indexName) {
        index(indexName, null);
    }

    /**
     * index
     *
     * @since 2020/5/26 15:15
     * @param indexName index name
     * @param alias alias name
     */
    public void index(String indexName, String alias) {
        try {
            CreateIndexRequest request = createIndexRequest(indexName);
            if (StringUtils.isNotBlank(alias)) {
                // 设置别名
                request.alias(new Alias(alias));
            }
            // Settings for this index
            request.settings(Settings.builder()
                    .put("index.number_of_shards", this.numberOfShards)
                    .put("index.number_of_replicas", this.numberOfReplicas));

            CreateIndexResponse response = client.indices().create(request, COMMON_OPTIONS);

            log.info(" whether all of the nodes have acknowledged the request : 「{}」", response.isAcknowledged());
            log.info(" Indicates whether the requisite number of shard copies were started for each shard in the index before timing out :「{}」",
                    response.isShardsAcknowledged());
        } catch (IOException e) {
            throw new ElasticsearchException("创建索引「{}」失败", indexName, e);
        }
    }

    /**
     * exists
     *
     * @since 2020/5/26 15:20
     * @param indexName index name
     * @return boolean index is exists
     */
    public boolean exists(String indexName) {
        return execute(client -> client.indices().exists(getIndexRequest(indexName), COMMON_OPTIONS));
    }

    /**
     * refresh
     *
     * @since 2020/5/27 15:29
     * @param indices indices
     */
    public void refresh(String ... indices) {
        Assert.notNull(indices, "No index defined for refresh()");
        execute(client -> client.indices().refresh(refreshRequest(indices), COMMON_OPTIONS));
    }

    /**
     * mapping
     *
     * @since 2020/5/26 15:48
     * @param indexName index name
     * @param sourceObject source object
     */
    public void mapping(String indexName, Object sourceObject) {
        mapping(indexName, null, sourceObject);
    }

    /**
     * mapping
     *
     * @since 2020/5/26 15:47
     * @param indexName index name
     * @param alias alias name
     * @param sourceObject source object
     */
    public void mapping(String indexName, String alias, Object sourceObject) {
        if (!exists(indexName)) {
            index(indexName, alias);
        }
        PutMappingRequest request = putMappingRequest(indexName);
        request.source(JsonUtils.isJsonString((String) sourceObject) ? (String) sourceObject : JsonUtils.toJson(sourceObject), XContentType.JSON);
        request.setTimeout(TimeValue.timeValueMinutes(2));
        request.setMasterTimeout(TimeValue.timeValueMinutes(2));
        execute(client -> client.indices().putMapping(request, COMMON_OPTIONS));
    }

    /**
     * delete
     *
     * @since 2020/5/26 16:19
     * @param indexName index name
     */
    public void delete(String indexName) {
        DeleteIndexRequest request = deleteIndexRequest(indexName);
        request.timeout(TimeValue.timeValueMinutes(3));
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        execute(client -> client.indices().delete(request, COMMON_OPTIONS));
    }

    /**
     * getSettings
     *
     * @since 2020/5/27 15:52
     * @param indexName index name
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String, Object> getSettings(String indexName) {
        return getSettings(indexName, true);
    }

    /**
     * getSettings
     *
     * @since 2020/5/27 15:49
     * @param indexName index name
     * @param includeDefaults is clude
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String, Object> getSettings(String indexName, boolean includeDefaults) {
        GetSettingsRequest request = getSettingsRequest().indices(indexName).includeDefaults(includeDefaults);
        GetSettingsResponse response = execute(client -> client.indices().getSettings(request, COMMON_OPTIONS));
        return convertSettingsResponseToMap(response, indexName);
    }

}
