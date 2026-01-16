package cn.fxbin.bubble.ai.constants;

/**
 * AI 模型常量定义
 *
 * @author fxbin
 */
public final class AiModelConstants {

    private AiModelConstants() {}

    /**
     * 缓存相关常量
     */
    public static class Cache {
        /**
         * 缓存键缓存最大数量
         */
        public static final int MAX_CACHE_KEY_CACHE_SIZE = 1000;
    }

    /**
     * 流式响应相关常量
     */
    public static class Streaming {
        /**
         * 流式响应最大缓冲区大小
         */
        public static final int MAX_STREAM_BUFFER_SIZE = 100000;
        /**
         * 流式响应最小采样大小
         */
        public static final int MIN_SAMPLE_SIZE = 1000;
    }

    /**
     * 模型相关常量
     */
    public static class Model {
        /**
         * 默认温度值
         */
        public static final double DEFAULT_TEMPERATURE = 0.7;
    }

    /**
     * Ollama 相关常量
     */
    public static class Ollama {
        /**
         * Ollama 默认 Base URL
         */
        public static final String DEFAULT_BASE_URL = "http://localhost:11434";
    }

    /**
     * 哈希相关常量
     */
    public static class Hash {
        /**
         * 默认哈希算法
         */
        public static final String DEFAULT_ALGORITHM = "SHA-256";
    }
}
