ALTER TABLE ai_model_config 
ADD COLUMN top_p DOUBLE PRECISION DEFAULT NULL;

COMMENT ON COLUMN ai_model_config.top_p IS 'TopP';