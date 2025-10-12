package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 知识图谱查询响应模型
 * 
 * <p>封装 LightRAG 系统查询操作的响应结果。与 OpenAPI 规范保持一致。</p>
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
public class QueryResponse implements Serializable {

    /**
     * 生成的响应内容
     * 
     * <p>基于知识图谱生成的查询答案。</p>
     */
    @JsonProperty("response")
    private String response;

    /**
     * 创建响应对象
     */
    public static QueryResponse of(String response) {
        return QueryResponse.builder()
                .response(response)
                .build();
    }

    /**
     * 检查是否有响应内容
     */
    public boolean hasResponse() {
        return response != null && !response.trim().isEmpty();
    }
}