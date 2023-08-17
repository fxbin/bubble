package cn.fxbin.bubble.data.elasticsearch.support;

import cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel;
import cn.fxbin.bubble.data.elasticsearch.exception.ElasticsearchException;
import cn.fxbin.bubble.core.util.BeanUtils;
import cn.hutool.core.date.SystemClock;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseElasticsearch
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/26 14:40
 */
@Slf4j
public class AbstractElasticsearchSupport {

    /**
     * rest 客户端
     */
    protected final RestHighLevelClient client;

    /**
     * 分片数量
     */
    protected final Integer numberOfShards;

    /**
     * 副本数量
     */
    protected final Integer numberOfReplicas;

    public AbstractElasticsearchSupport(RestHighLevelClient restHighLevelClient, Integer numberOfShards, Integer numberOfReplicas) {
        this.client = restHighLevelClient;
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
    }

    protected static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        // 默认缓冲限制为100MB，此处修改为150MB。
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory
                        .HeapBufferedResponseConsumerFactory(150 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    /**
     * indexRequest
     *
     * @since 2020/5/27 16:29
     * @param indexName index name
     * @return org.elasticsearch.action.index.IndexRequest
     */
    protected IndexRequest indexRequest(String indexName) {
        return new IndexRequest(indexName);
    }

    /**
     * indexRequest
     *
     * @since 2020/5/27 16:36
     * @param indexName index name
     * @param id id
     * @return org.elasticsearch.action.index.IndexRequest
     */
    protected IndexRequest indexRequest(String indexName, String id) {
        return new IndexRequest(indexName).id(id);
    }

    /**
     * indexRequest
     *
     * @since 2020/5/27 17:12
     * @param indexName index name
     * @param id id
     * @param source source
     * @return org.elasticsearch.action.index.IndexRequest
     */
    protected IndexRequest indexRequest(String indexName, String id, Object source) {
        return new IndexRequest(indexName).id(id).source(source instanceof Map? (Map<String, Object>) source : BeanUtils.object2Map(source, true), XContentType.JSON);
    }

    /**
     * createIndexRequest
     *
     * @since 2020/5/26 15:07
     * @param indexName index name
     * @return org.elasticsearch.client.indices.CreateIndexRequest
     */
    protected CreateIndexRequest createIndexRequest(String indexName) {
        return new CreateIndexRequest(indexName);
    }

    /**
     * getIndexRequest
     *
     * @since 2020/5/26 15:21
     * @param indexName index name
     * @return org.elasticsearch.client.indices.GetIndexRequest
     */
    protected GetIndexRequest getIndexRequest(String indexName) {
        return new GetIndexRequest(indexName);
    }

    /**
     * deleteIndexRequest
     *
     * @since 2020/5/26 16:12
     * @param indexName index
     * @return org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
     */
    protected DeleteIndexRequest deleteIndexRequest(String indexName) {
        return new DeleteIndexRequest(indexName);
    }

    /**
     * putMappingRequest
     *
     * @since 2020/6/15 11:01
     * @param indexName index
     * @return org.elasticsearch.client.indices.PutMappingRequest
     */
    protected PutMappingRequest putMappingRequest(String indexName) {
        return new PutMappingRequest(indexName);
    }

    /**
     * deleteRequest
     *
     * @since 2020/5/27 16:34
     * @param indexName index name
     * @return org.elasticsearch.action.delete.DeleteRequest
     */
    protected DeleteRequest deleteRequest(String indexName) {
        return new DeleteRequest(indexName);
    }

    /**
     * deleteRequest
     *
     * @since 2020/5/27 16:35
     * @param indexName indexName
     * @param id id
     * @return org.elasticsearch.action.delete.DeleteRequest
     */
    protected DeleteRequest deleteRequest(String indexName, String id) {
        return new DeleteRequest(indexName).id(id);
    }

    /**
     * refreshRequest
     *
     * @since 2020/5/26 16:23
     * @param indices indices
     * @return org.elasticsearch.action.admin.indices.refresh.RefreshRequest
     * @see org.elasticsearch.client.IndicesAdminClient#refresh(RefreshRequest)
     */
    protected RefreshRequest refreshRequest(String... indices) {
        return new RefreshRequest(indices);
    }

    /**
     * getSettingsRequest
     *
     * @since 2020/5/27 15:46
     * @return org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest
     */
    protected GetSettingsRequest getSettingsRequest() {
        return new GetSettingsRequest();
    }

    /**
     * updateRequest
     *
     * @since 2020/5/27 16:37
     * @return org.elasticsearch.action.update.UpdateRequest
     */
    protected UpdateRequest updateRequest() {
        return new UpdateRequest();
    }

    /**
     * updateRequest
     *
     * @since 2020/5/27 16:36
     * @param indexName index name
     * @param id id
     * @return org.elasticsearch.action.update.UpdateRequest
     */
    protected UpdateRequest updateRequest(String indexName, String id) {
        return new UpdateRequest(indexName, id);
    }

    /**
     * updateRequest
     *
     * @since 2020/5/27 17:14
     * @param indexName index name
     * @param id id
     * @param source source
     * @return org.elasticsearch.action.update.UpdateRequest
     */
    protected UpdateRequest updateRequest(String indexName, String id, Object source) {
        return new UpdateRequest(indexName, id).doc(source instanceof Map? (Map<String, Object>) source : BeanUtils.object2Map(source, true), XContentType.JSON);
    }

    /**
     * bulkRequest
     *
     * @since 2020/5/26 15:03
     * @param indexName index name
     * @return org.elasticsearch.action.bulk.BulkRequest
     */
    protected BulkRequest bulkRequest(String indexName) {
        return new BulkRequest(indexName);
    }

    /**
     * bulkRequest
     *
     * @since 2020/5/27 17:51
     * @param requestModel cn.fxbin.bubble.fireworks.data.elasticsearch.model.EsRequestModel
     * @return org.elasticsearch.action.bulk.BulkRequest
     */
    protected <T> BulkRequest bulkRequest(EsRequestModel requestModel) {
        return bulkRequest(requestModel, IndexRequest.class);
    }


    /**
     * bulkRequest
     *
     * @since 2020/5/27 17:15
     * @param requestModel cn.fxbin.bubble.fireworks.data.elasticsearch.model.EsRequestModel
     * @param requestClass request class, like IndexRequest,DeleteRequest,UpdateRequest
     * @return org.elasticsearch.action.bulk.BulkRequest
     * @see IndexRequest
     * @see DeleteRequest
     * @see UpdateRequest
     */
    protected <T> BulkRequest bulkRequest(EsRequestModel requestModel, Class<T> requestClass) {
        long startNs = SystemClock.now();

        String indexName = requestModel.getIndexName();
        BulkRequest bulkRequest = bulkRequest(indexName);

        requestModel.getDataList().forEach(docSource -> {
            if (requestClass.equals(IndexRequest.class)) {
                bulkRequest.add(indexRequest(indexName, docSource.getId(), docSource.getSource()));
            } else if (requestClass.equals(DeleteRequest.class)) {
                bulkRequest.add(deleteRequest(indexName, docSource.getId()));
            } else if (requestClass.equals(UpdateRequest.class)) {
                bulkRequest.add(updateRequest(indexName, docSource.getId(), docSource.getSource()));
            }
        });

        log.info("bulk「{}」data build consuming「{}」ms", requestModel.getIndexName(), (SystemClock.now() - startNs));

        return bulkRequest;
    }


    /**
     * Execute a callback with the {@link RestHighLevelClient}
     *
     * @param callback the callback to execute, must not be {@literal null}
     * @param <T> the type returned from the callback
     * @return the callback result
     * @link org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate#execute
     */
    public <T> T execute(ClientCallback<T> callback) {

        Assert.notNull(callback, "callback must not be null");

        try {
            return callback.doWithClient(client);
        } catch (IOException | RuntimeException e) {
            throw new ElasticsearchException("elasticsearch operation error", e);
        }
    }

    /**
     * Callback interface to be used with {@link #execute(ClientCallback)} for operating directly on
     * {@link RestHighLevelClient}.
     *
     * @link org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate.ClientCallback
     */
    @FunctionalInterface
    public interface ClientCallback<T> {
        /**
         * doWithClient
         *
         * @since 2020/5/28 11:17
         * @param client org.elasticsearch.client.RestHighLevelClient
         * @return T
         */
        T doWithClient(RestHighLevelClient client) throws IOException;
    }

    /**
     * convertSettingsResponseToMap
     *
     * @since 2020/5/27 15:51
     * @param response org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse
     * @param indexName index name
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    protected Map<String, Object> convertSettingsResponseToMap(GetSettingsResponse response, String indexName) {

        Map<String, Object> settings = new HashMap<>();

        if (!response.getIndexToDefaultSettings().isEmpty()) {
            Settings defaultSettings = response.getIndexToDefaultSettings().get(indexName);
            for (String key : defaultSettings.keySet()) {
                settings.put(key, defaultSettings.get(key));
            }
        }

        if (!response.getIndexToSettings().isEmpty()) {
            Settings customSettings = response.getIndexToSettings().get(indexName);
            for (String key : customSettings.keySet()) {
                settings.put(key, customSettings.get(key));
            }
        }

        return settings;
    }

}
