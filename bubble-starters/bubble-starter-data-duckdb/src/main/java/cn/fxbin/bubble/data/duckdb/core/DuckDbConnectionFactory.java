package cn.fxbin.bubble.data.duckdb.core;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.duckdb.DuckDBDriver;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * DuckDB 连接工厂
 *
 * <p>
 * 用于为 DuckDB 创建 HikariDataSource 实例的工厂。
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 15:12
 */
@Slf4j
public class DuckDbConnectionFactory {

    private final DuckDbProperties defaultProperties;

    public DuckDbConnectionFactory(DuckDbProperties defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    /**
     * 根据默认属性创建主数据源。
     *
     * @return 配置好的 HikariDataSource。
     */
    public HikariDataSource createDefaultDataSource() {
        String jdbcUrl;
        if (StringUtils.hasText(defaultProperties.getUrl())) {
            jdbcUrl = defaultProperties.getUrl();
        } else {
            StringBuilder urlBuilder = new StringBuilder("jdbc:duckdb:");
            
            if (defaultProperties.getMode() == DuckDbProperties.Mode.FILE) {
                if (!StringUtils.hasText(defaultProperties.getFilePath())) {
                    throw new IllegalArgumentException("在 FILE 模式下必须提供 DuckDB filePath。");
                }
                urlBuilder.append(defaultProperties.getFilePath());
            }
            
            // 添加只读标志
            if (defaultProperties.isReadOnly()) {
                urlBuilder.append("?duckdb.read_only=true");
            }
            
            jdbcUrl = urlBuilder.toString();
        }
        
        HikariDataSource dataSource = createBaseDataSource(jdbcUrl);
        dataSource.setPoolName("DuckDB-HikariPool");
        
        // 如果未指定，保留 Hikari 的默认值
        if (defaultProperties.getMaximumPoolSize() != null) {
            dataSource.setMaximumPoolSize(defaultProperties.getMaximumPoolSize());
        }

        // 初始化扩展
        initializeExtensions(dataSource);

        return dataSource;
    }

    /**
     * 为给定路径和模式创建新的数据源。
     *
     * @param filePath DuckDB 文件的路径。
     * @param readOnly 是否以只读模式打开。
     * @return 配置好的 HikariDataSource。
     */
    public HikariDataSource createDataSource(String filePath, boolean readOnly) {
        StringBuilder urlBuilder = new StringBuilder("jdbc:duckdb:");
        urlBuilder.append(filePath);
        
        // 添加只读标志
        if (readOnly) {
            urlBuilder.append("?duckdb.read_only=true");
        }
        
        HikariDataSource dataSource = createBaseDataSource(urlBuilder.toString());
        
        // 动态实例通常不需要大的连接池。
        // 如果用户没有为动态池指定全局默认值，则使用 1 或 2。
        Integer poolSize = defaultProperties.getMaximumPoolSize();
        dataSource.setMaximumPoolSize(poolSize != null ? poolSize : 2); 
        
        dataSource.setPoolName("DuckDB-Dynamic-" + filePath.hashCode());

        // 初始化扩展
        initializeExtensions(dataSource);
        
        return dataSource;
    }

    private HikariDataSource createBaseDataSource(String jdbcUrl) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(DuckDBDriver.class.getName());
        dataSource.setJdbcUrl(jdbcUrl);

        // 1. 应用核心性能配置 (通过 DataSource 属性传递给驱动，确保对所有连接生效)
        if (StringUtils.hasText(defaultProperties.getTempDirectory())) {
            dataSource.addDataSourceProperty("temp_directory", defaultProperties.getTempDirectory());
        }
        if (StringUtils.hasText(defaultProperties.getMemoryLimit())) {
            dataSource.addDataSourceProperty("memory_limit", defaultProperties.getMemoryLimit());
        }
        if (StringUtils.hasText(defaultProperties.getThreads())) {
            dataSource.addDataSourceProperty("threads", defaultProperties.getThreads());
        }

        // 应用插入顺序保留配置（默认 false 以减少内存使用）
        if (defaultProperties.getPreserveInsertionOrder() != null) {
            dataSource.addDataSourceProperty("preserve_insertion_order",
                    defaultProperties.getPreserveInsertionOrder().toString());
        }

        // 2. 应用扩展相关配置
        if (defaultProperties.isAllowUnsignedExtensions()) {
            dataSource.addDataSourceProperty("allow_unsigned_extensions", "true");
        }
        if (StringUtils.hasText(defaultProperties.getCustomExtensionRepository())) {
            dataSource.addDataSourceProperty("custom_extension_repository", defaultProperties.getCustomExtensionRepository());
        }

        // 3. 应用自定义配置
        if (defaultProperties.getConfig() != null) {
            defaultProperties.getConfig().forEach(dataSource::addDataSourceProperty);
        }

        dataSource.setConnectionTestQuery("SELECT 1");

        String initSql = buildConnectionInitSql();
        if (StringUtils.hasText(initSql)) {
            dataSource.setConnectionInitSql(initSql);
        }
        return dataSource;
    }

    private String buildConnectionInitSql() {
        Set<String> extensionsToLoad = resolveRequiredExtensions();
        List<DuckDbProperties.Attachment> enabledAttachments = resolveEnabledAttachments();
        boolean noExtensions = extensionsToLoad.isEmpty();
        boolean noAttachments = enabledAttachments.isEmpty();
        if (noExtensions && noAttachments) {
            return null;
        }

        StringBuilder sql = new StringBuilder();
        if (!noExtensions) {
            for (String ext : extensionsToLoad) {
                sql.append("LOAD ").append(ext).append("; ");
            }
        }
        if (!noAttachments) {
            for (DuckDbProperties.Attachment attachment : enabledAttachments) {
                sql.append(buildAttachSql(attachment)).append("; ");
            }
        }
        return sql.toString().trim();
    }

    private void initializeExtensions(DataSource dataSource) {
        Set<String> requiredExtensions = resolveRequiredExtensions();
        if (requiredExtensions.isEmpty()) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 安装并加载扩展
            for (String safeExt: requiredExtensions) {
                if (defaultProperties.isAutoInstallExtensions()) {
                    installExtension(stmt, safeExt);
                }
                
                log.info("Loading DuckDB extension: {}", safeExt);
                stmt.execute("LOAD " + safeExt);
            }
        } catch (SQLException e) {
            log.error("Failed to initialize DuckDB extensions", e);
            throw new RuntimeException("Failed to initialize DuckDB extensions", e);
        }
    }

    private Set<String> resolveRequiredExtensions() {
        Set<String> requiredExtensions = new LinkedHashSet<>();
        if (defaultProperties.getExtensions() != null) {
            for (String ext : defaultProperties.getExtensions()) {
                requiredExtensions.add(validateExtensionName(ext));
            }
        }
        for (DuckDbProperties.Attachment attachment : resolveEnabledAttachments()) {
            if (attachment.getType() == DuckDbProperties.AttachmentType.SQLITE) {
                requiredExtensions.add("sqlite");
            }
            if (attachment.getType() == DuckDbProperties.AttachmentType.MYSQL) {
                requiredExtensions.add("mysql");
            }
            if (attachment.getType() == DuckDbProperties.AttachmentType.POSTGRESQL) {
                requiredExtensions.add("postgres");
            }
        }
        return requiredExtensions;
    }

    private List<DuckDbProperties.Attachment> resolveEnabledAttachments() {
        if (defaultProperties.getAttachments() == null || defaultProperties.getAttachments().isEmpty()) {
            return List.of();
        }
        List<DuckDbProperties.Attachment> enabledAttachments = new ArrayList<>();
        for (DuckDbProperties.Attachment attachment : defaultProperties.getAttachments()) {
            if (attachment != null && attachment.isEnabled()) {
                enabledAttachments.add(attachment);
            }
        }
        return enabledAttachments;
    }

    private void installExtension(Statement stmt, String ext) {
        String installCmd;
        
        // 检查是否配置了本地扩展目录
        if (StringUtils.hasText(defaultProperties.getLocalExtensionDirectory())) {
            // 假设文件名格式为 name.duckdb_extension
            // 注意：需要处理路径分隔符
            String directory = defaultProperties.getLocalExtensionDirectory();
            String localPath = Path.of(directory, ext + ".duckdb_extension").toString();
            log.info("Installing DuckDB extension from local path: {}", localPath);
            installCmd = "INSTALL '" + escapeSingleQuotes(localPath) + "'";
        } else {
            log.info("Installing DuckDB extension: {}", ext);
            installCmd = "INSTALL " + ext;
        }

        try {
            stmt.execute(installCmd);
        } catch (SQLException e) {
            // 某些环境可能无法联网，或者扩展已预装。
            // 如果安装失败，我们记录警告但尝试继续加载，因为可能已经存在。
            log.warn("Failed to install DuckDB extension: {}. It might be already installed or not found. Error: {}", ext, e.getMessage());
        }
    }

    private static String validateExtensionName(String extensionName) {
        if (!StringUtils.hasText(extensionName)) {
            throw new IllegalArgumentException("扩展名不能为空");
        }

        String trimmed = extensionName.trim();
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("无效的扩展名：" + extensionName);
        }
        return trimmed;
    }

    private static String buildAttachSql(DuckDbProperties.Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("attachments 元素不能为空");
        }
        String connection = resolveAttachmentConnection(attachment);
        String alias = validateIdentifier(attachment.getAlias(), "attachments.alias");
        String escapedConnection = escapeSingleQuotes(connection);
        DuckDbProperties.AttachmentType attachmentType =
                attachment.getType() == null ? DuckDbProperties.AttachmentType.DUCKDB : attachment.getType();

        List<String> options = new ArrayList<>();
        if (attachmentType != DuckDbProperties.AttachmentType.DUCKDB) {
            options.add("TYPE " + toAttachTypeName(attachmentType));
        }
        if (attachment.isReadOnly()) {
            options.add("READ_ONLY");
        }
        if (attachmentType == DuckDbProperties.AttachmentType.SQLITE && attachment.isSqliteAllVarchar()) {
            options.add("SQLITE_ALL_VARCHAR");
        }
        if (attachment.getOptions() != null) {
            for (Map.Entry<String, String> entry : attachment.getOptions().entrySet()) {
                if (!StringUtils.hasText(entry.getKey()) || !StringUtils.hasText(entry.getValue())) {
                    continue;
                }
                String key = validateIdentifier(entry.getKey(), "attachments.options.key").toUpperCase(Locale.ROOT);
                options.add(key + " " + formatAttachOptionValue(entry.getValue()));
            }
        }

        if (options.isEmpty()) {
            return "ATTACH '" + escapedConnection + "' AS " + alias;
        }
        return "ATTACH '" + escapedConnection + "' AS " + alias + " (" + String.join(", ", options) + ")";
    }

    private static String resolveAttachmentConnection(DuckDbProperties.Attachment attachment) {
        if (StringUtils.hasText(attachment.getConnection())) {
            return attachment.getConnection().trim();
        }
        if (StringUtils.hasText(attachment.getPath())) {
            return attachment.getPath().trim();
        }
        throw new IllegalArgumentException("attachments.connection 不能为空");
    }

    private static String toAttachTypeName(DuckDbProperties.AttachmentType type) {
        return switch (type) {
            case SQLITE -> "SQLITE";
            case MYSQL -> "MYSQL";
            case POSTGRESQL -> "POSTGRES";
            case DUCKDB -> "DUCKDB";
        };
    }

    private static String formatAttachOptionValue(String value) {
        String trimmed = value.trim();
        if (trimmed.matches("^(?i:true|false)$")) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        if (trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
            return trimmed;
        }
        return "'" + escapeSingleQuotes(trimmed) + "'";
    }

    private static String validateIdentifier(String identifier, String fieldName) {
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        String trimmed = identifier.trim();
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("无效的 " + fieldName + "：" + identifier);
        }
        return trimmed;
    }

    private static String escapeSingleQuotes(String value) {
        return value.replace("'", "''");
    }
}
