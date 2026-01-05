CREATE TABLE `ai_model_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(64) NOT NULL COMMENT '配置名称',
  `platform` varchar(32) NOT NULL COMMENT '平台(openai, deepseek, siliconflow, ollama, gemini, anthropic, zhipu, minimax)',
  `api_key` varchar(256) DEFAULT NULL COMMENT 'API Key',
  `base_url` varchar(256) DEFAULT NULL COMMENT 'Base URL',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名称',
  `description` varchar(255) DEFAULT NULL COMMENT '模型描述',
  `temperature` double DEFAULT 0.7 COMMENT '温度',
  `top_k` int DEFAULT NULL COMMENT 'TopK',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_name` (`config_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';
