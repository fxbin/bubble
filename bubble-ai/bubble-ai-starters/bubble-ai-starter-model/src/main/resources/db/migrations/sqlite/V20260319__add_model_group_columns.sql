ALTER TABLE ai_model_config ADD COLUMN group_id TEXT DEFAULT NULL;
ALTER TABLE ai_model_config ADD COLUMN fallback_to_group_enabled INTEGER DEFAULT 1;
ALTER TABLE ai_model_config ADD COLUMN priority INTEGER DEFAULT 100;

CREATE INDEX IF NOT EXISTS idx_group_id_priority ON ai_model_config (group_id, priority);
