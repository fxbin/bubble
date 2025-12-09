# bubble-starter-data-duckdb

## 简介

`bubble-starter-data-duckdb` 是 Bubble 框架对 DuckDB 的深度集成模块。DuckDB 是一个高性能的进程内 SQL OLAP 数据库管理系统。本模块提供了开箱即用的配置、统一的操作入口 `DuckDbOperations` 以及对 Parquet 文件的便捷支持。

## 功能特性

- **零配置启动**：默认使用内存模式，无需繁琐配置即可使用。
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

默认情况下，模块使用内存数据库 (`:memory:`)。如果需要持久化到文件，可以在 `application.yml` 中配置：

```yaml
bubble:
  data:
    duckdb:
      # 默认连接的数据库文件路径，不填默认为 :memory:
      url: /path/to/my_db.duckdb
      # 是否只读
      read-only: false
      # 连接池配置
      maximum-pool-size: 10
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

模块抛出的异常统一封装为 `DataAccessException` (Spring JDBC) 或 `ServiceException` (Bubble 框架)，便于统一捕获处理。
