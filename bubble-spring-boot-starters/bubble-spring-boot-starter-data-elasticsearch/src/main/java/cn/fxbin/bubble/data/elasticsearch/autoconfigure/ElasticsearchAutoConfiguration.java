package cn.fxbin.bubble.data.elasticsearch.autoconfigure;

import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.data.elasticsearch.support.AbstractElasticsearchSupport;
import cn.fxbin.bubble.data.elasticsearch.support.DocumentOperations;
import cn.fxbin.bubble.data.elasticsearch.support.IndexOperations;
import lombok.AllArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * ElasticSearchAutoConfigure
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/20 18:03
 */
@Configuration(
        proxyBeanMethods = false
)
@AllArgsConstructor
@ConditionalOnProperty(prefix = ElasticsearchProperties.BUBBLE_FIREWORKS_ELASTICSEARCH_PREFIX, name = "cluster-nodes", matchIfMissing = false)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    private final ElasticsearchProperties properties;

    private final List<HttpHost> httpHosts = new ArrayList<>();

    @Bean(name = "restHighLevelClient", destroyMethod = "close")
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient() {

        List<String> clusterNodes = properties.getClusterNodes();
        clusterNodes.forEach(node -> {
            try {
                String[] parts = StringUtils.splitToArray(node, ":");
                Assert.notNull(parts, "Must defined Cluster Node");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");

                httpHosts.add(new HttpHost(parts[0], Integer.parseInt(parts[1]), properties.getSchema()));
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Invalid ES nodes " + "property '" + node + "'", e);
            }
        });
        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));

        return getRestHighLevelClient(builder, properties);
    }

    @Bean(name = {"documentOperations"})
    AbstractElasticsearchSupport documentOperations(@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient) {
        return new DocumentOperations(restHighLevelClient, properties.getIndex().getNumberOfShards(), properties.getIndex().getNumberOfReplicas());
    }

    @Bean(name = {"indexOperations"})
    AbstractElasticsearchSupport indexOperations(@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient) {
        return new IndexOperations(restHighLevelClient, properties.getIndex().getNumberOfShards(), properties.getIndex().getNumberOfReplicas());
    }

    /**
     * get restHistLevelClient
     *
     * @author fxbin
     * @param builder                 RestClientBuilder
     * @param elasticsearchProperties elasticsearch default properties
     * @return {@link org.elasticsearch.client.RestHighLevelClient}
     * @link <a>https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.6/_basic_authentication.html</a>
     */
    private static RestHighLevelClient getRestHighLevelClient(RestClientBuilder builder, ElasticsearchProperties elasticsearchProperties) {

        // Callback used the default {@link RequestConfig} being set to the {@link CloseableHttpClient}
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearchProperties.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(elasticsearchProperties.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(elasticsearchProperties.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });

        // Callback used to customize the {@link CloseableHttpClient} instance used by a {@link RestClient} instance.
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnectPerRoute());
            return httpClientBuilder;
        });

        // Callback used the basic credential auth
        ElasticsearchProperties.Account account = elasticsearchProperties.getAccount();
        if (!StringUtils.isEmpty(account.getUsername()) && !StringUtils.isEmpty(account.getUsername())) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(account.getUsername(), account.getPassword()));

            // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.6/_basic_authentication.html
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider);
            });
        }


        return new RestHighLevelClient(builder);
    }


}
