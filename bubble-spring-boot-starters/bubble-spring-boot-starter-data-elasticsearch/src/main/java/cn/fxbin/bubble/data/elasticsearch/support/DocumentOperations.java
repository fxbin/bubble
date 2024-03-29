package cn.fxbin.bubble.data.elasticsearch.support;

import cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel;
import cn.hutool.core.date.SystemClock;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ElasticsearchOperations
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/25 14:30
 */
@Slf4j
public class DocumentOperations extends AbstractElasticsearchSupport {

    public DocumentOperations(RestHighLevelClient restHighLevelClient, Integer numberOfShards, Integer numberOfReplicas) {
        super(restHighLevelClient, numberOfShards, numberOfReplicas);
    }

    /**
     * bulkIndex
     *
     * @since 2020/5/27 18:08
     * @param requestModel cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel
     */
    public void bulkIndex(EsRequestModel requestModel) {
        bulkOperation(requestModel, IndexRequest.class);
    }

    /**
     * bulkUpdate
     *
     * @since 2020/5/27 18:08
     * @param requestModel cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel
     */
    public void bulkUpdate(EsRequestModel requestModel) {
        bulkOperation(requestModel, UpdateRequest.class);
    }

    /**
     * bulkDelete
     *
     * @since 2020/5/27 18:08
     * @param requestModel cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel
     */
    public void bulkDelete(EsRequestModel requestModel) {
        bulkOperation(requestModel, DeleteRequest.class);
    }

    /**
     * bulkDelete
     *
     * @since 2020/5/28 15:52
     * @param indexName index name
     * @param docIds doc ids
     */
    public void bulkDelete(String indexName, List<String> docIds) {
        List<EsRequestModel.DocSource> docSourceList = docIds.stream().map(id -> EsRequestModel.DocSource.builder().id(id).build()).collect(Collectors.toList());
        EsRequestModel requestModel = EsRequestModel.builder().indexName(indexName).dataList(docSourceList).build();
        bulkDelete(requestModel);
    }

    /**
     * bulkOperation
     *
     * @since 2020/5/27 18:04
     * @param requestModel cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel
     * @param requestClass @link cn.fxbin.bubble.data.elasticsearch.support.AbstractElasticsearchSupport#bulkRequest(cn.fxbin.bubble.data.elasticsearch.model.EsRequestModel, java.lang.Class)
     */
    public void bulkOperation(EsRequestModel requestModel, Class<?> requestClass) {
        bulkRequestExecute(bulkRequest(requestModel, requestClass), requestModel);
    }

    /**
     * bulkRequestExecute
     *
     * @since 2020/5/27 18:00
     * @param bulkRequest org.elasticsearch.action.bulk.BulkRequest
     */
    public void bulkRequestExecute(BulkRequest bulkRequest, EsRequestModel requestModel) {
        long startNs = SystemClock.now();
        BulkResponse bulkResponse = execute(client -> client.bulk(bulkRequest, COMMON_OPTIONS));
        log.info("bulk index 「{}」 data consuming「{}」ms", requestModel.getIndexName(), (SystemClock.now() - startNs));
        bulkResponse.forEach(bulkItemResponse -> {
            if (bulkItemResponse.isFailed()) {
                log.warn("bulk failure message: 「{}」, index is 「{}」, id is 「{}」", bulkItemResponse.getFailureMessage(),
                        bulkItemResponse.getIndex(), bulkItemResponse.getId());
            }

            DocWriteResponse itemResponse = bulkItemResponse.getResponse();
            switch (bulkItemResponse.getOpType()) {
                case INDEX:
                case CREATE:
                    IndexResponse indexResponse = (IndexResponse) itemResponse;
                    log.info("创建索引|新增数据成功, index:「{}」, id :「{}」", indexResponse.getIndex(), indexResponse.getId());
                    break;
                case UPDATE:
                    UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                    log.info("修改数据成功, index:「{}」, id :「{}」", updateResponse.getIndex(), updateResponse.getId());
                    break;
                case DELETE:
                    DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                    log.info("删除数据成功, index:「{}」, id :「{}」", deleteResponse.getIndex(), deleteResponse.getId());
                    break;
                default:
            }
        });
    }

}
