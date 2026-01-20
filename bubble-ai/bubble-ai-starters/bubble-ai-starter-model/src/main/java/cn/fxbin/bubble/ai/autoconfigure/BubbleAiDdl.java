package cn.fxbin.bubble.ai.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.ddl.IDdl;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bubble AI DDL 自动维护
 * <p>负责自动执行数据库 schema 和迁移脚本</p>
 *
 * @author fxbin
 * @since 2026/01/05
 */
@Slf4j
public class BubbleAiDdl implements IDdl {

    private static final String MIGRATION_TABLE_NAME = "db_migration_history";

    private static final Pattern VERSION_PATTERN = Pattern.compile("V(\\d{8})__(.+)\\.sql");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    private JdbcTemplate jdbcTemplate;

    public BubbleAiDdl() {
    }

    public BubbleAiDdl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<String> getSqlFiles() {
        DbType dbType = DbType.MYSQL;
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            DatabaseMetaData metaData = con.getMetaData();
            dbType = JdbcUtils.getDbType(metaData.getURL());
        } catch (SQLException e) {
            log.warn("无法确定数据库类型，默认使用 mysql: {}", e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }

        String dbTypeStr = dbType.getDb();
        String script = "db/schema-" + dbTypeStr + ".sql";
        log.info("Bubble AI DDL 自动维护: 检测到数据库类型 [{}], 将执行脚本 [{}]", dbType, script);
        return Collections.singletonList(script);
    }

    @Override
    public void runScript(Consumer<DataSource> consumer) {
        if (jdbcTemplate == null && dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }
        consumer.accept(dataSource);
        migrate();
    }

    /**
     * 执行数据库迁移
     * <p>在应用启动时自动调用，检查并执行未执行的迁移脚本</p>
     */
    public void migrate() {
        try {
            DbType dbType = detectDatabaseType();
            log.info("检测到数据库类型: {}", dbType);

            createMigrationTableIfNotExists();

            List<MigrationScript> pendingScripts = getPendingScripts(dbType);
            if (pendingScripts.isEmpty()) {
                log.info("没有待执行的迁移脚本");
                return;
            }

            log.info("发现 {} 个待执行的迁移脚本", pendingScripts.size());
            for (MigrationScript script : pendingScripts) {
                executeScript(script);
            }

            log.info("数据库迁移完成");
        } catch (Exception e) {
            log.error("数据库迁移失败", e);
            throw new RuntimeException("数据库迁移失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检测数据库类型
     *
     * @return 数据库类型
     */
    private DbType detectDatabaseType() {
        try (Connection con = dataSource.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            return JdbcUtils.getDbType(metaData.getURL());
        } catch (SQLException e) {
            log.warn("无法确定数据库类型，默认使用 mysql: {}", e.getMessage());
            return DbType.MYSQL;
        }
    }

    /**
     * 创建迁移记录表（如果不存在）
     */
    private void createMigrationTableIfNotExists() {
        DbType dbType = detectDatabaseType();
        String createTableSql = getCreateMigrationTableSql(dbType);

        try {
            jdbcTemplate.execute(createTableSql);
            log.debug("迁移记录表已就绪");
        } catch (Exception e) {
            log.warn("创建迁移记录表失败（可能已存在）: {}", e.getMessage());
        }
    }

    /**
     * 获取创建迁移记录表的 SQL
     *
     * @param dbType 数据库类型
     * @return 创建表的 SQL
     */
    private String getCreateMigrationTableSql(DbType dbType) {
        return switch (dbType) {
            case POSTGRE_SQL -> "CREATE TABLE IF NOT EXISTS " + MIGRATION_TABLE_NAME + " (" +
                    "id SERIAL PRIMARY KEY, " +
                    "version VARCHAR(8) NOT NULL UNIQUE, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            case SQLITE -> "CREATE TABLE IF NOT EXISTS " + MIGRATION_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "version TEXT NOT NULL UNIQUE, " +
                    "name TEXT NOT NULL, " +
                    "executed_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            default -> "CREATE TABLE IF NOT EXISTS " + MIGRATION_TABLE_NAME + " (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "version VARCHAR(8) NOT NULL UNIQUE, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "executed_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        };
    }

    /**
     * 获取待执行的迁移脚本
     *
     * @param dbType 数据库类型
     * @return 待执行的迁移脚本列表
     */
    private List<MigrationScript> getPendingScripts(DbType dbType) {
        List<MigrationScript> allScripts = loadAllScripts(dbType);
        List<String> executedVersions = getExecutedVersions();

        return allScripts.stream()
                .filter(script -> !executedVersions.contains(script.getVersion()))
                .sorted(Comparator.comparing(MigrationScript::getVersion))
                .toList();
    }

    /**
     * 加载所有迁移脚本
     *
     * @param dbType 数据库类型
     * @return 所有迁移脚本列表
     */
    private List<MigrationScript> loadAllScripts(DbType dbType) {
        List<MigrationScript> scripts = new ArrayList<>();
        String dbTypeStr = dbType.getDb();
        String locationPattern = "classpath:db/migrations/" + dbTypeStr + "/*.sql";

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
            Resource[] resources = resolver.getResources(locationPattern);

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    Matcher matcher = VERSION_PATTERN.matcher(filename);
                    if (matcher.matches()) {
                        String version = matcher.group(1);
                        String name = matcher.group(2);
                        scripts.add(new MigrationScript(version, name, resource));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("加载迁移脚本失败: {}", e.getMessage());
        }

        return scripts;
    }

    /**
     * 获取已执行的迁移版本
     *
     * @return 已执行的版本列表
     */
    private List<String> getExecutedVersions() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT version FROM " + MIGRATION_TABLE_NAME + " ORDER BY version",
                    String.class
            );
        } catch (Exception e) {
            log.warn("查询已执行的迁移版本失败（可能是首次运行）: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 执行迁移脚本
     *
     * @param script 迁移脚本
     */
    private void executeScript(MigrationScript script) {
        log.info("开始执行迁移脚本: V{}__{}.sql", script.getVersion(), script.getName());

        try {
            String sqlContent = readScriptContent(script.getResource());
            jdbcTemplate.execute(sqlContent);

            recordMigration(script);

            log.info("迁移脚本执行成功: V{}__{}.sql", script.getVersion(), script.getName());
        } catch (Exception e) {
            log.error("迁移脚本执行失败: V{}__{}.sql", script.getVersion(), script.getName(), e);
            throw new RuntimeException("迁移脚本执行失败: " + script.getName(), e);
        }
    }

    /**
     * 读取脚本内容
     *
     * @param resource 资源文件
     * @return 脚本内容
     */
    private String readScriptContent(Resource resource) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 记录迁移执行
     *
     * @param script 迁移脚本
     */
    private void recordMigration(MigrationScript script) {
        jdbcTemplate.update(
                "INSERT INTO " + MIGRATION_TABLE_NAME + " (version, name, executed_at) VALUES (?, ?, ?)",
                script.getVersion(),
                script.getName(),
                LocalDateTime.now()
        );
    }

    /**
     * 迁移脚本内部类
     */
    @Getter
    @RequiredArgsConstructor
    private static class MigrationScript implements Serializable {

        private final String version;

        private final String name;

        private final Resource resource;
    }
}
