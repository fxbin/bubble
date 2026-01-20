ALTER TABLE `ai_model_config` 
ADD COLUMN `top_p` double DEFAULT NULL COMMENT 'TopP' AFTER `top_k`;