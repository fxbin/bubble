# bubble-starter-data-duckdb

## 简介

`bubble-starter-data-duckdb` 是 Bubble 框架对 DuckDB 的深度集成模块。DuckDB 是一个高性能的进程内 SQL OLAP 数据库管理系统。本模块提供了开箱即用的配置、统一的操作入口 `DuckDbOperations` 以及对 Parquet 文件的便捷支持。

## 功能特性

- **开箱即用**：提供 `DataSource`（命名为 `duckDbDataSource`）、`DuckDbTemplate`、`DuckDbIngester`、`DuckDbManager` 与统一入口 `DuckDbOperations`。
- **统一操作入口**：通过 `DuckDbOperations` 门面类，统一暴露 `DuckDbTemplate` (JDBC操作)、`DuckDbManager` (多实例管理) 和 `DuckDbIngester` (高性能导入)。
- **Parquet 支持**：内置 Parquet 文件的导入与导出功能。
- **多实例管理**：支持动态连接多个 DuckDB 数据库文件。
- **高性能写入**：支持 Appender 模式的高性能数据追加。

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-data-duckdb</artifactId>
</dependency>
```

### 2. 配置 (可选)

自动配置开关与属性前缀为 `dm.data.duckdb`（参考：`bubble-starters/bubble-starter-data-duckdb/src/main/java/cn/fxbin/bubble/data/duckdb/autoconfigure/DuckDbProperties.java:19`）。

默认行为：

- 默认启用：`dm.data.duckdb.enabled=true`（参考：`DuckDbProperties.java:25`）
- 默认模式：`FILE`（参考：`DuckDbProperties.java:30`）
- 默认文件：`duckdb_store.db`（参考：`DuckDbProperties.java:36`）
- 默认内存限制：`2GB`（参考：`DuckDbProperties.java:107`）
- 默认线程数：`Runtime.getRuntime().availableProcessors()`（参考：`DuckDbProperties.java:113`）

示例（文件模式，推荐用于本地落盘/分析任务）：

```yaml
dm:
  data:
    duckdb:
      enabled: true
      mode: FILE
      file-path: ./duckdb_store.db
      read-only: false
      maximum-pool-size: 2
      memory-limit: 2GB
      threads: 4

      # 可选：扩展（需要时开启，避免测试/离线环境触发安装）
      # extensions:
      #   - json
      # auto-install-extensions: true
      # allow-unsigned-extensions: false
      # custom-extension-repository: https://your-mirror/
      # local-extension-directory: /path/to/extensions/
```

### 3. 使用 DuckDbOperations

注入 `DuckDbOperations` 即可开始使用：

```java
@Service
@Slf4j
public class DataService {

    @Resource
    private DuckDbOperations duckDbOperations;

    public void demo() {
        // 1. 建表
        duckDbOperations.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER, name VARCHAR)");

        // 2. 插入数据
        duckDbOperations.execute("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob')");

        // 3. 查询列表
        List<Map<String, Object>> users = duckDbOperations.query("SELECT * FROM users");
        log.info("Users: {}", users);

        // 4. 查询单个对象
        Long count = duckDbOperations.queryForObject("SELECT count(*) FROM users", Long.class);
        log.info("User count: {}", count);
        
        // 5. 导出为 Parquet
        duckDbOperations.exportParquet("users", "/tmp/users.parquet");
        
        // 6. 导入 Parquet
        duckDbOperations.importParquet("users_new", "/tmp/users.parquet");
    }
}
```

注意：`exportParquet` / `importParquet` 使用了 `read_parquet` 与 `COPY ... (FORMAT PARQUET)`（参考：`bubble-starters/bubble-starter-data-duckdb/src/main/java/cn/fxbin/bubble/data/duckdb/core/DuckDbTemplate.java:68`、`DuckDbTemplate.java:86`）。若运行环境缺少对应能力，需要通过 `dm.data.duckdb.extensions` 配置加载扩展。

## 高级特性

### 动态多数据源

如果需要同时操作多个 DuckDB 文件，可以使用 `connect` 方法：

```java
// 连接另一个数据库文件
DuckDbTemplate otherDb = duckDbOperations.connect("/path/to/other.db");

// 在该连接上执行操作
otherDb.execute("CREATE TABLE test (id INTEGER)");

// 关闭连接
duckDbOperations.close("/path/to/other.db");
```

动态连接的缓存键包含读写模式（参考：`bubble-starters/bubble-starter-data-duckdb/src/main/java/cn/fxbin/bubble/data/duckdb/core/DuckDbManager.java:72`）。

### 高性能数据摄入 (Ingestion)

对于海量数据写入，推荐使用 `ingest` 方法：

```java
List<Object[]> rows = new ArrayList<>();
rows.add(new Object[]{1, "Data1"});
rows.add(new Object[]{2, "Data2"});

// 使用 Appender 模式追加数据
duckDbOperations.append("users", rows);
```

## 异常处理

异常行为以代码为准：

- `DuckDbTemplate` 对非法表名会抛 `IllegalArgumentException`（参考：`DuckDbTemplate.java:91`）
- `DuckDbConnectionFactory` 在 FILE 模式缺少 `filePath` 会抛 `IllegalArgumentException`（参考：`DuckDbConnectionFactory.java:47`）
- `DuckDbIngester` 将 `SQLException` 统一包装为 `RuntimeException` 抛出（参考：`DuckDbIngester.java:128`）
