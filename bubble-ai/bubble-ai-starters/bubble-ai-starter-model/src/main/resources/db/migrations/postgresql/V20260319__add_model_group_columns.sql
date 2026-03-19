ALTER TABLE ai_model_config
ADD COLUMN group_id VARCHAR(64) DEFAULT NULL,
ADD COLUMN fallback_to_group_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN priority INTEGER DEFAULT 100;

CREATE INDEX IF NOT EXISTS idx_group_id_priority ON ai_model_config (group_id, priority);

COMMENT ON COLUMN ai_model_config.group_id IS '所属模型组ID';
COMMENT ON COLUMN ai_model_config.fallback_to_group_enabled IS '是否允许自动切换到同组其他模型';
COMMENT ON COLUMN ai_model_config.priority IS '同组内优先级，值越小优先级越高';
