CREATE TABLE IF NOT EXISTS ai_model_group (
    id BIGSERIAL PRIMARY KEY,
    group_code VARCHAR(64) NOT NULL,
    group_name VARCHAR(128) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    model_type VARCHAR(32) DEFAULT 'CHAT',
    failover_enabled BOOLEAN DEFAULT TRUE,
    cooldown_seconds INTEGER DEFAULT 30,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_group_code ON ai_model_group (group_code);

CREATE TABLE IF NOT EXISTS ai_model_config (
    id BIGSERIAL PRIMARY KEY,
    config_name VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    api_key VARCHAR(256) DEFAULT NULL,
    base_url VARCHAR(256) DEFAULT NULL,
    model VARCHAR(64) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    temperature DOUBLE PRECISION DEFAULT 0.7,
    top_k INTEGER DEFAULT NULL,
    top_p DOUBLE PRECISION DEFAULT NULL,
    group_id VARCHAR(64) DEFAULT NULL,
    fallback_to_group_enabled BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 100,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_config_name ON ai_model_config (config_name);
CREATE INDEX idx_group_id_priority ON ai_model_config (group_id, priority);

COMMENT ON TABLE ai_model_group IS 'AI模型分组表';
COMMENT ON COLUMN ai_model_group.id IS '主键ID';
COMMENT ON COLUMN ai_model_group.group_code IS '模型组编码';
COMMENT ON COLUMN ai_model_group.group_name IS '模型组名称';
COMMENT ON COLUMN ai_model_group.description IS '模型组描述';
COMMENT ON COLUMN ai_model_group.model_type IS '模型类型';
COMMENT ON COLUMN ai_model_group.failover_enabled IS '是否启用组级故障切换';
COMMENT ON COLUMN ai_model_group.cooldown_seconds IS '故障冷却秒数';
COMMENT ON COLUMN ai_model_group.enabled IS '是否启用';
COMMENT ON COLUMN ai_model_group.create_time IS '创建时间';
COMMENT ON COLUMN ai_model_group.update_time IS '更新时间';

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
COMMENT ON COLUMN ai_model_config.top_p IS 'TopP';
COMMENT ON COLUMN ai_model_config.group_id IS '所属模型组编码';
COMMENT ON COLUMN ai_model_config.fallback_to_group_enabled IS '是否允许自动切换到同组其他模型';
COMMENT ON COLUMN ai_model_config.priority IS '同组内优先级，值越小优先级越高';
COMMENT ON COLUMN ai_model_config.enabled IS '是否启用';
COMMENT ON COLUMN ai_model_config.create_time IS '创建时间';
COMMENT ON COLUMN ai_model_config.update_time IS '更新时间';
