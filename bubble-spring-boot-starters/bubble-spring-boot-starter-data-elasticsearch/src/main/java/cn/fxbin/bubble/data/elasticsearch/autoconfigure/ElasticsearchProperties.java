package cn.fxbin.bubble.data.elasticsearch.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.fxbin.bubble.data.elasticsearch.autoconfigure.ElasticsearchProperties.BUBBLE_ELASTICSEARCH_PREFIX;

/**
 * ElasticsearchProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/20 18:28
 */
@Data
@ConfigurationProperties(prefix = BUBBLE_ELASTICSEARCH_PREFIX)
public class ElasticsearchProperties {

    /**
     * elasticsearch prefix
     */
    public static final String BUBBLE_ELASTICSEARCH_PREFIX = "bubble.elasticsearch";

    /**
     * 请求协议
     */
    private String schema = "http";

    /**
     * 集群名称，默认"elasticsearch"
     */
    private String clusterName = "elasticsearch";

    /**
     * 集群节点
     */
    @NonNull
    private List<String> clusterNodes = new ArrayList<>(Collections.singletonList("localhost:9200"));

    /**
     * 连接超时时间(毫秒)
     */
    private Integer connectTimeout = 1000;

    /**
     * socket 超时时间
     */
    private Integer socketTimeout = 30000;

    /**
     * 连接请求超时时间
     */
    private Integer connectionRequestTimeout = 500;

    /**
     * 每个路由的最大连接数量
     */
    private Integer maxConnectPerRoute = 10;

    /**
     * 最大连接总数量
     */
    private Integer maxConnectTotal = 30;

    /**
     * 索引配置信息
     */
    private Index index = new Index();

    /**
     * 认证账户
     */
    private Account account = new Account();

    /**
     * 索引配置信息
     */
    @Data
    public static class Index {

        /**
         * 分片数量
         */
        private Integer numberOfShards = 3;

        /**
         * 副本数量
         */
        private Integer numberOfReplicas = 1;

    }

    /**
     * 认证账户
     */
    @Data
    public static class Account {

        /**
         * 认证用户
         */
        private String username;

        /**
         * 认证密码
         */
        private String password;

    }
}
