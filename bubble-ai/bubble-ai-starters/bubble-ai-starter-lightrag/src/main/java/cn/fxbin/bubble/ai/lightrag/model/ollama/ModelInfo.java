package cn.fxbin.bubble.ai.lightrag.model.ollama;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * TagInfo
 *
 * <p>示例数据：
 * <pre>
 * {
 *   "models": [
 *     {
 *       "name": "lightrag:latest",
 *       "model": "lightrag:latest",
 *       "size": 7365960935,
 *       "digest": "sha256:lightrag",
 *       "modified_at": "2024-01-15T00:00:00Z",
 *       "details": {
 *         "parent_model": "",
 *         "format": "gguf",
 *         "family": "lightrag",
 *         "families": [
 *           "lightrag"
 *         ],
 *         "parameter_size": "13B",
 *         "quantization_level": "Q4_0"
 *       }
 *     }
 *   ]
 * }
 * </pre>
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 16:43
 */
@Data
public class ModelInfo implements Serializable {

    private Model[] models;

    public static class Model implements Serializable {

        private String name;

        private String model;

        private long size;

        private String digest;

        @JsonProperty("modified_at")
        private String modifiedAt;

        private ModelDetails details;

        @Data
        public static class ModelDetails implements Serializable {
            @JsonProperty("parent_model")
            private String parentModel;
            private String format;
            private String family;
            private String[] families;

            @JsonProperty("parameter_size")
            private String parameterSize;

            @JsonProperty("quantization_level")
            private String quantizationLevel;
        }

    }

}
