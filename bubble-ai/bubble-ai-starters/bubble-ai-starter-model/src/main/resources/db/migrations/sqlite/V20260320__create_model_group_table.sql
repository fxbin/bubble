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
