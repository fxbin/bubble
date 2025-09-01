package cn.fxbin.bubble.ai.lightrag.model.ollama;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * OllamaChatResponse
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 15:44
 */
@Data
public class OllamaChatResponse implements Serializable {

    private String model;

    @JsonProperty("created_at")
    private String createdAt;

    private OllamaMessage message;

    private boolean done;

}
