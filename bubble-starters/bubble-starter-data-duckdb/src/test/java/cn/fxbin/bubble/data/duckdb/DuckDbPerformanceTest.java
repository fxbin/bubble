package cn.fxbin.bubble.data.duckdb;

import cn.fxbin.bubble.data.duckdb.autoconfigure.DuckDbProperties;
import cn.fxbin.bubble.data.duckdb.core.DuckDbIngester;
import cn.fxbin.bubble.data.duckdb.core.DuckDbManager;
import cn.fxbin.bubble.data.duckdb.core.DuckDbTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DuckDB 性能测试 (IoT 场景)
 *
 * <p>
 * 测试 100 列 IoT 数据的高性能写入和查询。
 * </p>
 *
 * @author fxbin
 * @version v2.0
 * @since 2025/12/09
 */
@Slf4j
public class DuckDbPerformanceTest {

    private DuckDbManager duckDbManager;
    private DuckDbOperations duckDbOps;
    private String dbPath;
    private String csvPath;

    /**
     * 测试数据量
     * 默认 100,000 用于快速验证。
     */
    private static final long DATA_COUNT = 100_000L;

    @BeforeEach
    void setUp() {
        // 使用临时文件
        dbPath = "target/duckdb_iot_perf.db";
        csvPath = "target/iot_data_perf.csv";

        // 清理旧文件
        try {
            Files.deleteIfExists(Paths.get(dbPath));
            Files.deleteIfExists(Paths.get(csvPath));
        } catch (Exception e) {
            log.warn("Failed to clean files: {}", e.getMessage());
        }

        // 初始化 Manager
        DuckDbProperties properties = new DuckDbProperties();
        properties.setMode(DuckDbProperties.Mode.FILE);
        properties.setFilePath(dbPath);
        properties.setMemoryLimit("2GB");

        duckDbManager = new DuckDbManager(properties);
        DuckDbTemplate template = duckDbManager.getTemplate(dbPath);
        DuckDbIngester ingester = new DuckDbIngester(template.getDataSource());

        // 初始化客户端
        duckDbOps = new DuckDbOperations(template, duckDbManager, ingester);
    }

    @AfterEach
    void tearDown() {
        if (duckDbManager != null) {
            duckDbManager.close();
        }
    }

    /**
     * 核心性能测试: 生成 -> 导入 -> 查询
     */
    @Test
    void testPerformance() throws IOException {
        // 1. 创建表
        createTable(duckDbOps.template());

        // 2. 生成 CSV 数据
        log.info("正在生成 {} 行测试数据 (100 列)...", DATA_COUNT);
        long start = System.currentTimeMillis();
        generateCsvData(csvPath, DATA_COUNT);
        log.info("数据生成完成，耗时: {} ms", System.currentTimeMillis() - start);

        // 3. 导入数据 (COPY 模式)
        log.info("开始数据导入 (COPY 模式)...");
        start = System.currentTimeMillis();
        // 明确指定分隔符为逗号，且无表头
        String importSql = String.format("COPY iot_data FROM '%s' (DELIMITER ',', HEADER FALSE)", csvPath);
        duckDbOps.execute(importSql);
        log.info("COPY 导入完成，耗时: {} ms", System.currentTimeMillis() - start);

        // 4. 执行查询
        executePerformanceQueries(duckDbOps.template());
    }

    @Test
    void testAppenderIngestion() throws IOException {
        // 独立数据库文件
        String appenderDbPath = "target/duckdb_appender_iot.db";
        Path path = Paths.get(appenderDbPath);
        Files.deleteIfExists(path);

        DuckDbTemplate template = duckDbOps.connect(appenderDbPath);
        createTable(template);

        log.info("开始生成数据流...");
        // 模拟数据流
        Iterator<Object[]> dataStream = new Iterator<>() {
            private long current = 0;

            @Override
            public boolean hasNext() {
                return current < DATA_COUNT;
            }

            @Override
            public Object[] next() {
                current++;
                return generateRowData(current);
            }
        };

        log.info("开始数据导入 (Appender 模式)...");
        long start = System.currentTimeMillis();

        template.append("iot_data", dataStream);

        log.info("Appender 导入完成，耗时: {} ms", System.currentTimeMillis() - start);

        // 验证
        Long count = template.queryForObject("SELECT COUNT(*) FROM iot_data", Long.class);
        log.info("导入后计数: {}", count);
        if (count != DATA_COUNT) {
            throw new RuntimeException("Data count mismatch!");
        }
    }

    private void createTable(DuckDbTemplate template) {
        template.execute("DROP TABLE IF EXISTS iot_data");
        StringBuilder sql = new StringBuilder("CREATE TABLE iot_data (\n");

        // 1. ID (PK)
        sql.append("    id VARCHAR PRIMARY KEY,\n");

        // 2-10: 设备信息 (9 cols)
        sql.append("    device_id VARCHAR,\n");
        sql.append("    device_type VARCHAR,\n");
        sql.append("    vendor VARCHAR,\n");
        sql.append("    model VARCHAR,\n");
        sql.append("    firmware_ver VARCHAR,\n");
        sql.append("    serial_no VARCHAR,\n");
        sql.append("    batch_no VARCHAR,\n");
        sql.append("    purchase_date TIMESTAMP,\n");
        sql.append("    warranty_expire TIMESTAMP,\n");

        // 11-20: 位置信息 (10 cols)
        sql.append("    location_lat DOUBLE,\n");
        sql.append("    location_lon DOUBLE,\n");
        sql.append("    location_alt DOUBLE,\n");
        sql.append("    region_code VARCHAR,\n");
        sql.append("    city VARCHAR,\n");
        sql.append("    zone_id VARCHAR,\n");
        sql.append("    building_id VARCHAR,\n");
        sql.append("    floor_id VARCHAR,\n");
        sql.append("    room_id VARCHAR,\n");
        sql.append("    placement VARCHAR,\n");

        // 21-30: 状态信息 (10 cols)
        sql.append("    status VARCHAR,\n");
        sql.append("    online BOOLEAN,\n");
        sql.append("    battery_level INTEGER,\n");
        sql.append("    signal_dbm INTEGER,\n");
        sql.append("    ip_address VARCHAR,\n");
        sql.append("    mac_address VARCHAR,\n");
        sql.append("    last_boot TIMESTAMP,\n");
        sql.append("    uptime_seconds BIGINT,\n");
        sql.append("    error_code INTEGER,\n");
        sql.append("    warning_level INTEGER,\n");

        // 31-90: 传感器数据 (60 cols)
        for (int i = 1; i <= 20; i++) sql.append(String.format("    temp_%02d DOUBLE,\n", i));
        for (int i = 1; i <= 10; i++) sql.append(String.format("    humid_%02d DOUBLE,\n", i));
        for (int i = 1; i <= 10; i++) sql.append(String.format("    press_%02d DOUBLE,\n", i));
        for (int i = 1; i <= 10; i++) sql.append(String.format("    vib_%02d DOUBLE,\n", i));
        for (int i = 1; i <= 10; i++) sql.append(String.format("    elec_%02d DOUBLE,\n", i));

        // 91-100: 元数据 (10 cols)
        sql.append("    data_quality INTEGER,\n");
        sql.append("    processed BOOLEAN,\n");
        sql.append("    tags VARCHAR,\n");
        sql.append("    notes VARCHAR,\n");
        sql.append("    operator VARCHAR,\n");
        sql.append("    maintenance_due TIMESTAMP,\n");
        sql.append("    config_hash VARCHAR,\n");
        sql.append("    tenant_id VARCHAR,\n");
        sql.append("    created_at TIMESTAMP,\n");
        sql.append("    updated_at TIMESTAMP\n");

        sql.append(");");

        template.execute(sql.toString());
        log.info("表 'iot_data' 创建成功，包含 100 列。");
        
        // 创建索引
        template.execute("CREATE INDEX idx_device_id ON iot_data(device_id)");
        template.execute("CREATE INDEX idx_created_at ON iot_data(created_at)");
        template.execute("CREATE INDEX idx_location ON iot_data(city, region_code)");
        log.info("索引创建成功。");
    }

    private Object[] generateRowData(long i) {
        Object[] row = new Object[100];
        int col = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 1. ID
        row[col++] = randomString(16);

        // 2-10: 设备信息
        row[col++] = "DEV_" + random.nextInt(1, 1000); // device_id
        row[col++] = randomEle(new String[]{"Sensor", "Gateway", "Robot", "Camera"}); // device_type
        row[col++] = "Vendor_" + (char)('A' + random.nextInt(26)); // vendor
        row[col++] = "Model_" + random.nextInt(1, 10); // model
        row[col++] = "v" + random.nextInt(1, 5) + "." + random.nextInt(0, 9); // firmware
        row[col++] = randomString(10); // serial
        row[col++] = "BATCH-" + (2020 + random.nextInt(0, 5)); // batch
        row[col++] = LocalDateTime.now().minusDays(random.nextInt(0, 1000)); // purchase
        row[col++] = LocalDateTime.now().plusDays(random.nextInt(0, 1000)); // warranty

        // 11-20: 位置信息
        row[col++] = random.nextDouble(20.0, 50.0); // lat
        row[col++] = random.nextDouble(100.0, 130.0); // lon
        row[col++] = random.nextDouble(0.0, 100.0); // alt
        row[col++] = "CN"; // region
        row[col++] = randomEle(new String[]{"Beijing", "Shanghai", "Shenzhen", "Hangzhou"}); // city
        row[col++] = "Z" + random.nextInt(1, 10); // zone
        row[col++] = "B" + random.nextInt(1, 20); // building
        row[col++] = "F" + random.nextInt(1, 50); // floor
        row[col++] = "R" + random.nextInt(101, 999); // room
        row[col++] = randomEle(new String[]{"Ceiling", "Wall", "Floor"}); // placement

        // 21-30: 状态信息
        row[col++] = randomEle(new String[]{"Active", "Standby", "Error", "Maintenance"}); // status
        row[col++] = random.nextBoolean(); // online
        row[col++] = random.nextInt(0, 100); // battery
        row[col++] = random.nextInt(-100, -30); // signal
        row[col++] = "192.168." + random.nextInt(0, 255) + "." + random.nextInt(0, 255); // ip
        row[col++] = randomString(12); // mac
        row[col++] = LocalDateTime.now().minusHours(random.nextInt(0, 100)); // last_boot
        row[col++] = random.nextLong(0, 1000000); // uptime
        row[col++] = random.nextInt(0, 5); // error_code
        row[col++] = random.nextInt(0, 3); // warning_level

        // 31-90: 传感器数据 (60 cols)
        for (int k = 0; k < 20; k++) row[col++] = random.nextDouble(20.0, 80.0);
        for (int k = 0; k < 10; k++) row[col++] = random.nextDouble(30.0, 90.0);
        for (int k = 0; k < 10; k++) row[col++] = random.nextDouble(900.0, 1100.0);
        for (int k = 0; k < 10; k++) row[col++] = random.nextDouble(0.0, 5.0);
        for (int k = 0; k < 10; k++) row[col++] = random.nextDouble(210.0, 230.0);

        // 91-100: 元数据
        row[col++] = random.nextInt(1, 10); // quality
        row[col++] = random.nextBoolean(); // processed
        // 注意：CSV 中使用逗号作为分隔符，所以数据中不能包含逗号，或者需要转义。
        // 这里为了简单，将 tag 分隔符改为分号。
        row[col++] = "tag1;tag2"; // tags
        row[col++] = "Routine check"; // notes
        row[col++] = "Admin"; // operator
        row[col++] = LocalDateTime.now().plusMonths(3); // maintenance
        row[col++] = randomString(8); // hash
        row[col++] = "T-" + random.nextInt(100, 200); // tenant
        row[col++] = LocalDateTime.now().minusSeconds(random.nextInt(0, 3600)); // created_at
        row[col++] = LocalDateTime.now(); // updated_at

        return row;
    }

    private String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    private <T> T randomEle(T[] array) {
        if (array == null || array.length == 0) return null;
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    private void generateCsvData(String path, long count) throws IOException {
        Path filePath = Paths.get(path);
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (long i = 0; i < count; i++) {
                Object[] rowData = generateRowData(i);
                List<String> csvRow = new ArrayList<>(100);
                for (Object val : rowData) {
                    if (val == null) {
                        csvRow.add("");
                    } else if (val instanceof LocalDateTime) {
                        csvRow.add(((LocalDateTime) val).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    } else {
                        csvRow.add(String.valueOf(val));
                    }
                }
                writer.write(String.join(",", csvRow));
                writer.newLine();

                if (i % 50000 == 0 && i > 0) {
                    log.info("已生成 {} 行...", i);
                }
            }
        }
    }

    private void executePerformanceQueries(DuckDbTemplate template) {
        log.info("=== 开始执行性能查询 ===");

        // 1. 计数
        runQuery(template, "1. 总数统计", "SELECT COUNT(*) FROM iot_data");

        // 2. 分组
        runQuery(template, "2. 设备类型统计", 
            "SELECT device_type, COUNT(*) as cnt, AVG(temp_01) as avg_temp FROM iot_data GROUP BY device_type ORDER BY cnt DESC");

        // 3. 过滤 + 排序 (走索引)
        runQuery(template, "3. 最近告警 (索引)", 
            "SELECT device_id, created_at, error_code FROM iot_data WHERE error_code > 0 ORDER BY created_at DESC LIMIT 100");

        // 4. 复杂过滤 (非索引)
        runQuery(template, "4. 高温低电量 (全表扫描)", 
            "SELECT device_id, temp_01, battery_level FROM iot_data WHERE temp_01 > 75.0 AND battery_level < 20 LIMIT 100");

        // 5. 多列聚合
        runQuery(template, "5. 传感器平均值", 
            "SELECT AVG(temp_01), AVG(humid_01), AVG(press_01), AVG(vib_01), AVG(elec_01) FROM iot_data");

        // 6. 分页
        runQuery(template, "6. 分页查询 (Offset 50000)", 
            "SELECT device_id, created_at FROM iot_data ORDER BY created_at LIMIT 100 OFFSET 50000");
        
        // 7. 窗口函数
        runQuery(template, "7. 窗口函数 (排名)",
            "SELECT device_id, temp_01, RANK() OVER (PARTITION BY device_type ORDER BY temp_01 DESC) as rank FROM iot_data LIMIT 100");

        log.info("=== 性能测试结束 ===");
    }

    private void runQuery(DuckDbTemplate template, String name, String sql) {
        try {
            long start = System.currentTimeMillis();
            template.execute(sql);
            long cost = System.currentTimeMillis() - start;
            log.info("[{}] 耗时: {} ms | SQL: {}", name, cost, sql);
        } catch (Exception e) {
            log.error("[{}] 失败: {}", name, e.getMessage());
        }
    }
}
