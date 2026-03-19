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
