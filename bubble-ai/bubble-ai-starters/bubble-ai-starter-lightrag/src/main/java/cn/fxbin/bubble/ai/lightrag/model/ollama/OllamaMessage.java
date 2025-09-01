package cn.fxbin.bubble.ai.lightrag.model.ollama;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * OllamaMessage
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/28 15:43
 */
@Data
public class OllamaMessage implements Serializable {

    private String role;
    private String content;
    private List<String> images;

}
