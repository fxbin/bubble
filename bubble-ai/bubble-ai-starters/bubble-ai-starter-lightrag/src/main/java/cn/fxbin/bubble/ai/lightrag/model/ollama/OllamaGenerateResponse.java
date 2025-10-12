package cn.fxbin.bubble.ai.lightrag.model.ollama;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * OllamaGenerateResponse
 *
 *
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 15:40
 */
@Data
public class OllamaGenerateResponse implements Serializable {

    private String model;
    @JsonProperty("created_at")
    private String createdAt;

    private String response;
    private boolean done;
    private List<Integer> context;
    @JsonProperty("total_duration")
    private Long totalDuration;
    @JsonProperty("load_duration")
    private Long loadDuration;
    @JsonProperty("prompt_eval_count")
    private Integer promptEvalCount;
    @JsonProperty("prompt_eval_duration")
    private Long promptEvalDuration;
    @JsonProperty("eval_count")
    private Integer evalCount;
    @JsonProperty("eval_duration")
    private Long evalDuration;


}
