package cn.fxbin.bubble.ai.lightrag.model.ollama;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * OllamaGenerateRequest
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 15:39
 */
@Data
public class OllamaGenerateRequest implements Serializable {

    private String model;

    private String prompt;

    private String system;

    private boolean stream;

    private Map<String, Object> options;

}
