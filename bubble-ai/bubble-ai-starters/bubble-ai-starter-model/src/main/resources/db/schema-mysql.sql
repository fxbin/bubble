CREATE TABLE `ai_model_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_code` varchar(64) NOT NULL COMMENT '模型组编码',
  `group_name` varchar(128) NOT NULL COMMENT '模型组名称',
  `description` varchar(255) DEFAULT NULL COMMENT '模型组描述',
  `model_type` varchar(32) DEFAULT 'CHAT' COMMENT '模型类型',
  `failover_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用组级故障切换',
  `cooldown_seconds` int DEFAULT 30 COMMENT '故障冷却秒数',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型分组表';

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
  `top_p` double DEFAULT NULL COMMENT 'TopP',
  `group_id` varchar(64) DEFAULT NULL COMMENT '所属模型组编码',
  `fallback_to_group_enabled` tinyint(1) DEFAULT 1 COMMENT '是否允许自动切换到同组其他模型',
  `priority` int DEFAULT 100 COMMENT '同组内优先级，值越小优先级越高',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_name` (`config_name`),
  KEY `idx_group_id_priority` (`group_id`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';
