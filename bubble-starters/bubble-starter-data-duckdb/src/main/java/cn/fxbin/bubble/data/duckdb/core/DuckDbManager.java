package cn.fxbin.bubble.data.duckdb.core;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DuckDB 管理器
 *
 * <p>
 * 管理动态 DuckDB 数据源和模板。
 * 允许在运行时为特定文件创建和访问 DuckDB 实例。
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 14:15
 */
@Slf4j
public class DuckDbManager implements Closeable {

    private final DuckDbConnectionFactory connectionFactory;
    
    /**
     * 数据源缓存：键是文件路径（或 ID），值是数据源。
     */
    private final Map<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();
    
    /**
     * 模板缓存：键是文件路径（或 ID），值是 DuckDbTemplate。
     */
    private final Map<String, DuckDbTemplate> templateCache = new ConcurrentHashMap<>();

    public DuckDbManager(DuckDbProperties properties) {
        this.connectionFactory = new DuckDbConnectionFactory(properties);
    }


    /**
     * 获取或为指定文件路径创建一个 DuckDbTemplate。
     * 默认为读写模式。
     *
     * @param filePath DuckDB 文件的路径。
     * @return DuckDbTemplate 实例。
     */
    public DuckDbTemplate getTemplate(String filePath) {
        return getTemplate(filePath, false);
    }

    /**
     * 获取或为指定文件路径和特定模式创建一个 DuckDbTemplate。
     *
     * @param filePath DuckDB 文件的路径。
     * @param readOnly 是否以只读模式打开。
     * @return DuckDbTemplate 实例。
     */
    public DuckDbTemplate getTemplate(String filePath, boolean readOnly) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        // 如果我们想支持以不同模式打开同一文件，是否应该使用复合键？
        // 通常，HikariCP 池绑定到一个 URL。
        // 读写模式 URL：jdbc:duckdb:/path
        // 只读模式 URL：jdbc:duckdb:/path?duckdb.read_only=true
        // 所以键应该包含模式。
        String cacheKey = buildCacheKey(filePath, readOnly);

        return templateCache.computeIfAbsent(cacheKey, k -> {
            HikariDataSource dataSource = getDataSource(filePath, readOnly);
            return new DuckDbTemplate(dataSource);
        });
    }

    /**
     * 获取或为指定文件路径创建一个数据源。
     *
     * @param filePath DuckDB 文件的路径。
     * @param readOnly 是否以只读模式打开。
     * @return HikariDataSource 实例。
     */
    public HikariDataSource getDataSource(String filePath, boolean readOnly) {
        String cacheKey = buildCacheKey(filePath, readOnly);
        
        return dataSourceCache.computeIfAbsent(cacheKey, k -> {
            log.info("为路径创建动态 DuckDB 数据源：{} (只读：{})", filePath, readOnly);
            return createDataSource(filePath, readOnly);
        });
    }

    /**
     * 关闭并移除指定文件路径的数据源。
     * 这对于释放文件锁很重要。
     *
     * @param filePath DuckDB 文件的路径。
     * @param readOnly 要关闭的模式。
     */
    public void close(String filePath, boolean readOnly) {
        String cacheKey = buildCacheKey(filePath, readOnly);
        
        templateCache.remove(cacheKey);
        HikariDataSource ds = dataSourceCache.remove(cacheKey);
        
        if (ds != null) {
            log.info("关闭路径的动态 DuckDB 数据源：{}", filePath);
            ds.close();
        }
    }

    /**
     * 关闭所有动态数据源。
     */
    @Override
    public void close() {
        log.info("关闭所有动态 DuckDB 数据源...");
        dataSourceCache.forEach((k, ds) -> ds.close());
        dataSourceCache.clear();
        templateCache.clear();
    }

    private String buildCacheKey(String filePath, boolean readOnly) {
        return filePath + "::" + (readOnly ? "RO" : "RW");
    }

    private HikariDataSource createDataSource(String filePath, boolean readOnly) {
        return connectionFactory.createDataSource(filePath, readOnly);
    }
}