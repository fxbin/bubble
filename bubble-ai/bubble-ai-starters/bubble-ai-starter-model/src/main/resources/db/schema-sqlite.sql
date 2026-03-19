CREATE TABLE IF NOT EXISTS ai_model_group (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_code TEXT NOT NULL UNIQUE,
    group_name TEXT NOT NULL,
    description TEXT DEFAULT NULL,
    model_type TEXT DEFAULT 'CHAT',
    failover_enabled INTEGER DEFAULT 1,
    cooldown_seconds INTEGER DEFAULT 30,
    enabled INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_model_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_name TEXT NOT NULL UNIQUE,
    platform TEXT NOT NULL,
    api_key TEXT DEFAULT NULL,
    base_url TEXT DEFAULT NULL,
    model TEXT DEFAULT NULL,
    description TEXT DEFAULT NULL,
    temperature REAL DEFAULT 0.7,
    top_k INTEGER DEFAULT NULL,
    top_p REAL DEFAULT NULL,
    group_id TEXT DEFAULT NULL,
    fallback_to_group_enabled INTEGER DEFAULT 1,
    priority INTEGER DEFAULT 100,
    enabled INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_group_id_priority ON ai_model_config (group_id, priority);
