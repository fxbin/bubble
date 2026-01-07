package cn.fxbin.bubble.data.duckdb.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DuckDB 配置属性
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 11:35
 */
@Data
@ConfigurationProperties(prefix = "dm.data.duckdb")
public class DuckDbProperties {

    /**
     * 是否启用 DuckDB 自动配置。
     */
    private boolean enabled = true;

    /**
     * 数据库模式：内存模式或文件模式。
     */
    private Mode mode = Mode.FILE;

    /**
     * 数据库文件路径（当模式为 FILE 时必需）。
     * 如果未指定，默认为工作目录中的 "duckdb_store.db"。
     */
    private String filePath = "duckdb_store.db";

    /**
     * 是否以只读模式打开数据库。
     * 在多进程场景中，当此实例为读取者时很有用。
     */
    private boolean readOnly = false;

    /**
     * 自定义 JDBC URL。如果设置，将覆盖 mode 和 filePath 设置。
     */
    private String url;

    /**
     * HikariCP 的最大连接池大小。
     * 对于内存中的 DuckDB，通常 1 个连接就足够了，但它支持并发连接。
     * 对于基于文件的数据库，它也支持来自同一进程的并发连接。
     */
    private Integer maximumPoolSize;

    /**
     * DuckDB 特定配置选项。
     * DuckDB 配置的键值对（例如：threads、max_memory）。
     */
    private Map<String, String> config = new HashMap<>();

    /**
     * 要加载的 DuckDB 扩展列表（例如：parquet, httpfs, json）。
     */
    private List<String> extensions = new ArrayList<>();

    /**
     * 是否自动安装扩展。
     * 如果为 true，将尝试运行 "INSTALL extension_name"。
     * 如果为 false，则仅运行 "LOAD extension_name"。
     */
    private boolean autoInstallExtensions = true;

    /**
     * 自定义扩展仓库 URL。
     * 用于内网环境，指向托管 DuckDB 扩展的 HTTP 服务器。
     * 对应 SQL: SET custom_extension_repository = '...';
     */
    private String customExtensionRepository;

    /**
     * 本地扩展目录路径。
     * 如果设置，且 autoInstallExtensions 为 true，将尝试从此目录安装扩展。
     * 文件名应为 {extension_name}.duckdb_extension。
     * 对应 SQL: INSTALL 'path/to/extension.duckdb_extension';
     */
    private String localExtensionDirectory;

    /**
     * 是否允许加载未签名的扩展。
     * 加载本地或自定义仓库的扩展时通常需要设置为 true。
     * 对应 SQL: SET allow_unsigned_extensions = true;
     */
    private boolean allowUnsignedExtensions = false;

    /**
     * 临时文件目录 (temp_directory)
     * 用于溢出到磁盘时的临时存储。建议指向高速磁盘（如 NVMe SSD）。
     */
    private String tempDirectory;

    /**
     * 最大内存限制 (memory_limit)
     * 例如: '10GB', '2GB'
     * 默认为 '2GB' (默认 1GB 在大数据量 Group By/Sort 时可能 OOM，提升至 2GB)
     */
    private String memoryLimit = "2GB";

    /**
     * 线程数 (threads)
     * 默认为 CPU 核心数。
     */
    private String threads = String.valueOf(Runtime.getRuntime().availableProcessors());

    /**
     * 是否保留插入顺序 (preserve_insertion_order)
     * 默认为 false，以减少内存使用。
     * 当设置为 true 时，DuckDB 会保持插入顺序，但会消耗更多内存。
     * 对于大数据量导入/查询，建议设置为 false 以提升性能。
     */
    private Boolean preserveInsertionOrder = false;

    /**
     * 数据库模式枚举
     */
    public enum Mode {
        /**
         * 内存数据库。进程退出时数据将丢失。
         */
        MEMORY,

        /**
         * 基于文件的数据库。数据将被持久化。
         */
        FILE
    }

}