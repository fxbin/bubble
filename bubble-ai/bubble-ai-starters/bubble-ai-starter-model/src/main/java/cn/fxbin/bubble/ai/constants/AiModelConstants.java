package cn.fxbin.bubble.ai.constants;

public final class AiModelConstants {

    private AiModelConstants() {}

    public static class Cache {
        public static final int MAX_CACHE_KEY_CACHE_SIZE = 1000;
    }

    public static class Streaming {
        public static final int MAX_STREAM_BUFFER_SIZE = 100000;
        public static final int MIN_SAMPLE_SIZE = 1000;
    }

    public static class Model {
        public static final double DEFAULT_TEMPERATURE = 0.7;
    }

    public static class Ollama {
        public static final String DEFAULT_BASE_URL = "http://localhost:11434";
    }

    public static class Hash {
        public static final String DEFAULT_ALGORITHM = "SHA-256";
    }
}
