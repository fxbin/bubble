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

CREATE UNIQUE INDEX IF NOT EXISTS uk_group_code ON ai_model_group (group_code);

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
