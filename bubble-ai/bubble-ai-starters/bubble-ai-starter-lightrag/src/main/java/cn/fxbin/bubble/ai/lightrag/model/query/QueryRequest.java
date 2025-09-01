package cn.fxbin.bubble.ai.lightrag.model.query;

import cn.fxbin.bubble.ai.lightrag.enums.ConversionRole;
import cn.fxbin.bubble.ai.lightrag.enums.QueryMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 知识图谱查询请求模型
 * 
 * <p>用于向 LightRAG 系统发起知识查询的请求参数。支持多种查询模式，
 * 包括本地查询、全局查询和混合查询，以满足不同场景的需求。</p>
 * 
 * <h3>请求示例：</h3>
 * <pre>{@code
 * {
 *   "mode": "hybrid",
 *   "response_type": "Bullet Points",
 *   "top_k": 40,
 *   "chunk_top_k": 10,
 *   "max_entity_tokens": 10000,
 *   "max_relation_tokens": 10000,
 *   "max_total_tokens": 32000,
 *   "only_need_context": false,
 *   "only_need_prompt": false,
 *   "stream": false,
 *   "history_turns": 0,
 *   "user_prompt": "",
 *   "enable_rerank": true,
 *   "query": "SQL ",
 *   "conversation_history": [
 *     {
 *       "role": "user",
 *       "content": "灵蹊"
 *     },
 *     {
 *       "role": "assistant",
 *       "content": "- 灵蹊（XiYan-SQL）是一个先进的多生成器集成的自然语言到SQL（NL2SQL）框架，旨在提升SQL查询生成的准确性和鲁棒性。  \n- 该框架采用两阶段、多任务训练策略：第一阶段激活基础SQL生成能力，第二阶段增强语义理解与风格多样性。  \n- 核心组件包括：Schema Linking（用于链接自然语言查询与数据库元素）、Candidate Generator（生成多样化候选SQL）、以及Refiner（基于执行反馈修正逻辑或语法错误）。  \n- 为优化性能，灵蹊引入M-Schema表示法，以半结构化方式呈现数据库模式，提升模型对数据库结构的理解。  \n- 灵蹊在多个基准测试中表现优异：在Spider数据集上达到89.65%的执行准确率（排名第一），在Bird开发集上达到75.63%，在SQL-Eval上达69.86%，在非关系型数据库NL2GQL上达41.20%。  \n- 其候选选择机制通过专门微调的选择模型实现，优于传统的self-consistency方法。  \n- 灵蹊可访问官方网址：[https://bailian.console.aliyun.com/xiyan](https://bailian.console.aliyun.com/xiyan)。  \n\nReferences  \n[KG] XiYan-SQL中文版.pdf  \n[DC] XiYan-SQL中文版.pdf"
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <h3>查询模式说明：</h3>
 * <ul>
 *   <li>naive: 朴素查询，简单的文本匹配</li>
 *   <li>local: 本地查询，基于向量相似度检索</li>
 *   <li>global: 全局查询，基于知识图谱推理</li>
 *   <li>hybrid: 混合查询，结合本地和全局的优势</li>
 *   <li>mix: 混合查询，结合本地和全局，并使用混合策略</li>
 *   <li>bypass: 跳过查询，直接返回结果</li>
 * </ul>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-21 15:22:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryRequest implements Serializable {

    /**
     * 查询模式
     */
    private QueryMode mode = QueryMode.HYBRID;

    /**
     * 响应类型
     * Defines the response format. Examples: 'Multiple Paragraphs', 'Single Paragraph', 'Bullet Points'
     */
    @Builder.Default
    @JsonProperty("response_type")
    private ResponseType responseType = ResponseType.BulletPoints;

    /**
     * 返回结果的最大数量
     */
    @Builder.Default
    @JsonProperty("top_k")
    private Integer topK = 10;

    /**
     * 每个文档返回的最大结果数量
     */
    @Builder.Default
    @JsonProperty("chunk_top_k")
    private Integer chunkTopK = 10;

    /**
     * 实体最大Token数
     */
    @JsonProperty("max_entity_tokens")
    private Integer maxEntityTokens;

    /**
     * 关系最大Token数
     */
    @JsonProperty("max_relation_tokens")
    private Integer maxRelationTokens;

    /**
     * 最大总Token数
     */
    @JsonProperty("max_total_tokens")
    private Integer maxTotalTokens;

    /**
     * 是否只返回上下文
     */
    @Builder.Default
    @JsonProperty("only_need_context")
    private Boolean onlyNeedContext = false;

    /**
     * 是否只返回提示
     */
    @Builder.Default
    @JsonProperty("only_need_prompt")
    private Boolean onlyNeedPrompt = false;

    /**
     * 是否流式返回结果
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 历史对话轮数
     */
    @JsonProperty("history_turns")
    private Integer historyTurns;

    /**
     * 用户提示
     */
    @JsonProperty("user_prompt")
    private String userPrompt;

    /**
     * 是否启用rerank
     */
    @Builder.Default
    @JsonProperty("enable_rerank")
    private Boolean enableRerank = true;

    private String query;

    @JsonProperty("conversation_history")
    private List<Conversation> conversationHistory;


    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Conversation {

        private ConversionRole role;

        private String content;
    }

    @Getter
    @RequiredArgsConstructor
    public enum ResponseType {
        MultipleParagraphs("Multiple Paragraphs"),
        SingleParagraph("Single Paragraph"),
        BulletPoints("Bullet Points");

        @JsonValue
        private final String value;
    }

}