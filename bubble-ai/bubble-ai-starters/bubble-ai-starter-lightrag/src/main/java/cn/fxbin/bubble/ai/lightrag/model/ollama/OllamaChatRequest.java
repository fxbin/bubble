package cn.fxbin.bubble.ai.lightrag.model.ollama;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * OllamaChatRequest
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 15:40
 */
@Data
public class OllamaChatRequest implements Serializable {

    private String model;
    private List<OllamaMessage> messages;
    private boolean stream;
    private Map<String, Object> options;
    private String system;

}
