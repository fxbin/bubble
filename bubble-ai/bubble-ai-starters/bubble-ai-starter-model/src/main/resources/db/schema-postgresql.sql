CREATE TABLE IF NOT EXISTS ai_model_config (
    id SERIAL PRIMARY KEY,
    config_name VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    api_key VARCHAR(256) DEFAULT NULL,
    base_url VARCHAR(256) DEFAULT NULL,
    model VARCHAR(64) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    temperature DOUBLE PRECISION DEFAULT 0.7,
    top_k INTEGER DEFAULT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_config_name ON ai_model_config (config_name);

COMMENT ON TABLE ai_model_config IS 'AI模型配置表';
COMMENT ON COLUMN ai_model_config.id IS '主键ID';
COMMENT ON COLUMN ai_model_config.config_name IS '配置名称';
COMMENT ON COLUMN ai_model_config.platform IS '平台(openai, deepseek, siliconflow, ollama, gemini, anthropic, zhipu, minimax)';
COMMENT ON COLUMN ai_model_config.api_key IS 'API Key';
COMMENT ON COLUMN ai_model_config.base_url IS 'Base URL';
COMMENT ON COLUMN ai_model_config.model IS '模型名称';
COMMENT ON COLUMN ai_model_config.description IS '模型描述';
COMMENT ON COLUMN ai_model_config.temperature IS '温度';
COMMENT ON COLUMN ai_model_config.top_k IS 'TopK';
COMMENT ON COLUMN ai_model_config.enabled IS '是否启用';
COMMENT ON COLUMN ai_model_config.create_time IS '创建时间';
COMMENT ON COLUMN ai_model_config.update_time IS '更新时间';
