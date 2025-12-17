package cn.fxbin.bubble.data.duckdb.benchmark;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import cn.fxbin.bubble.data.duckdb.core.DuckDbConnectionFactory;
import cn.fxbin.bubble.data.duckdb.core.DuckDbIngester;
import cn.fxbin.bubble.data.duckdb.core.DuckDbTemplate;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StopWatch;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DuckDB 宽表性能基准测试
 *
 * @author fxbin
 * @since 2025/12/17
 */
@Slf4j
public class WideTableBenchmarkTest {

    private HikariDataSource dataSource;
    private DuckDbTemplate template;
    private DuckDbIngester ingester;

    // 配置参数
    private static final int COLUMN_COUNT = 2000;
    private static final int ROW_COUNT = 10_000; // 1万行 * 2000列 = 2000万个数据点
    private static final String TABLE_NAME = "wide_table_benchmark";

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        DuckDbProperties properties = new DuckDbProperties();
        // 使用文件模式以模拟真实 I/O，并设置合理的内存限制
        properties.setMode(DuckDbProperties.Mode.FILE);
        properties.setFilePath(tempDir.resolve("benchmark.duckdb").toString());
        properties.setMemoryLimit("4GB"); // 给足内存
        properties.setThreads("4");

        DuckDbConnectionFactory factory = new DuckDbConnectionFactory(properties);
        dataSource = factory.createDefaultDataSource();
        template = new DuckDbTemplate(dataSource);
        ingester = new DuckDbIngester(dataSource);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void benchmarkWideTableIngestAndQuery() {
        log.info("开始宽表性能测试：列数={}, 行数={}", COLUMN_COUNT, ROW_COUNT);

        // 1. 创建宽表
        createWideTable();

        // 2. 准备数据生成器
        Iterator<Object[]> dataIterator = new WideTableDataIterator(ROW_COUNT, COLUMN_COUNT);

        // 3. 执行写入测试
        StopWatch stopWatch = new StopWatch("DuckDB Benchmark");
        stopWatch.start("Ingest " + ROW_COUNT + " rows");
        
        ingester.ingest(TABLE_NAME, dataIterator, 2000); // 每 2000 行打一次日志
        
        stopWatch.stop();
        log.info("写入完成。耗时: {} ms, 吞吐量: {} rows/sec", 
                stopWatch.getLastTaskTimeMillis(), 
                (double) ROW_COUNT / stopWatch.getLastTaskTimeMillis() * 1000);

        // 4. 验证行数
        Long count = template.queryForObject("SELECT count(*) FROM " + TABLE_NAME, Long.class);
        assertThat(count).isEqualTo(ROW_COUNT);

        // 5. 执行查询测试 - 全表聚合
        stopWatch.start("Query Aggregation (COUNT)");
        template.queryForObject("SELECT count(*) FROM " + TABLE_NAME, Long.class);
        stopWatch.stop();
        
        // 6. 执行查询测试 - 特定列过滤与聚合 (模拟业务查询)
        // 查询 col_1 (DOUBLE 类型) 的平均值，过滤 col_0 > 0
        stopWatch.start("Query Complex Aggregation");
        // col_1 是 DOUBLE 类型，适合做聚合测试
        String querySql = "SELECT avg(col_1) FROM " + TABLE_NAME + " WHERE col_0 > 0";
        Double avg = template.queryForObject(querySql, Double.class);
        stopWatch.stop();
        log.info("查询结果 (AVG col_1): {}", avg);

        log.info(stopWatch.prettyPrint());
    }

    private void createWideTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + TABLE_NAME + " (");
        
        // 为了模拟真实场景，我们使用混合类型
        // col_0: BIGINT (ID)
        // col_1...N: 循环使用 INTEGER, DOUBLE, VARCHAR, TIMESTAMP
        
        sql.append("col_0 BIGINT");

        for (int i = 1; i < COLUMN_COUNT; i++) {
            sql.append(", ");
            sql.append("col_").append(i).append(" ");
            
            int typeMod = i % 4;
            switch (typeMod) {
                case 0 -> sql.append("INTEGER");
                case 1 -> sql.append("DOUBLE");
                case 2 -> sql.append("VARCHAR");
                case 3 -> sql.append("TIMESTAMP");
            }
        }
        sql.append(")");
        
        long start = System.currentTimeMillis();
        template.execute(sql.toString());
        log.info("建表完成。耗时: {} ms", System.currentTimeMillis() - start);
    }

    /**
     * 宽表数据生成器
     */
    static class WideTableDataIterator implements Iterator<Object[]> {
        private final int totalRows;
        private final int totalCols;
        private final AtomicInteger currentRow = new AtomicInteger(0);
        private final Random random = new Random();
        private final LocalDateTime now = LocalDateTime.now();

        public WideTableDataIterator(int totalRows, int totalCols) {
            this.totalRows = totalRows;
            this.totalCols = totalCols;
        }

        @Override
        public boolean hasNext() {
            return currentRow.get() < totalRows;
        }

        @Override
        public Object[] next() {
            int rowId = currentRow.getAndIncrement();
            Object[] row = new Object[totalCols];
            
            // col_0 is ID
            row[0] = (long) rowId;

            for (int i = 1; i < totalCols; i++) {
                int typeMod = i % 4;
                switch (typeMod) {
                    case 0 -> row[i] = rowId + i; // INTEGER
                    case 1 -> row[i] = (double) rowId * i / 100.0; // DOUBLE
                    case 2 -> row[i] = "str_" + rowId + "_" + i; // VARCHAR
                    case 3 -> row[i] = now.plusSeconds(rowId); // TIMESTAMP
                }
            }
            return row;
        }
    }
}
