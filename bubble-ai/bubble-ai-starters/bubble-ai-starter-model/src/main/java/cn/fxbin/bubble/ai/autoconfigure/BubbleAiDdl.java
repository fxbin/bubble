package cn.fxbin.bubble.ai.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.ddl.IDdl;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bubble AI DDL 自动维护
 *
 * @author fxbin
 * @since 2026/01/05
 */
@Slf4j
public class BubbleAiDdl implements IDdl {

    @Autowired
    private DataSource dataSource;

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
        consumer.accept(dataSource);
    }
}
