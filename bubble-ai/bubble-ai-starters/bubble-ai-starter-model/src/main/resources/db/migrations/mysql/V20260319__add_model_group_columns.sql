ALTER TABLE `ai_model_config`
ADD COLUMN `group_id` varchar(64) DEFAULT NULL COMMENT '所属模型组ID' AFTER `top_p`,
ADD COLUMN `fallback_to_group_enabled` tinyint(1) DEFAULT 1 COMMENT '是否允许自动切换到同组其他模型' AFTER `group_id`,
ADD COLUMN `priority` int DEFAULT 100 COMMENT '同组内优先级，值越小优先级越高' AFTER `fallback_to_group_enabled`,
ADD KEY `idx_group_id_priority` (`group_id`, `priority`);
