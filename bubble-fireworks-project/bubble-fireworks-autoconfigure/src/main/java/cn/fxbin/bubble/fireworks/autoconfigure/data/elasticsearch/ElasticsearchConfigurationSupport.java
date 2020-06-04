package cn.fxbin.bubble.fireworks.autoconfigure.data.elasticsearch;

import cn.fxbin.bubble.fireworks.data.elasticsearch.support.AbstractElasticsearchSupport;
import cn.fxbin.bubble.fireworks.data.elasticsearch.support.DocumentOperations;
import cn.fxbin.bubble.fireworks.data.elasticsearch.support.IndexOperations;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * ElasticsearchConfigurationSupport
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/27 18:19
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({AbstractElasticsearchSupport.class, DocumentOperations.class, IndexOperations.class})
@AutoConfigureAfter(ElasticsearchAutoConfiguration.class)
public class ElasticsearchConfigurationSupport {

    @Resource
    private ElasticsearchProperties properties;

    @Bean(name = {"documentOperations"})
    @ConditionalOnBean(name = "restHighLevelClient")
    AbstractElasticsearchSupport documentOperations(@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient) {
        return new DocumentOperations(restHighLevelClient, properties.getIndex().getNumberOfShards(), properties.getIndex().getNumberOfReplicas());
    }

    @Bean(name = {"indexOperations"})
    @ConditionalOnBean(name = "restHighLevelClient")
    AbstractElasticsearchSupport indexOperations(@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient) {
        return new IndexOperations(restHighLevelClient, properties.getIndex().getNumberOfShards(), properties.getIndex().getNumberOfReplicas());
    }

}
