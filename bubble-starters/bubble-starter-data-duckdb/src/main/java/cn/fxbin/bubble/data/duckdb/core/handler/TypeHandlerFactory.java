package cn.fxbin.bubble.data.duckdb.core.handler;

import cn.fxbin.bubble.core.util.time.DateUtils;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 类型处理器工厂
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/08 15:08
 */
public class TypeHandlerFactory {

    private static final Map<Integer, TypeHandler> HANDLERS = new HashMap<>();
    private static final TypeHandler DEFAULT_HANDLER = (appender, value) -> appender.append(value == null ? null : value.toString());

    static {
        register(Types.INTEGER, (appender, value) -> {
            if (value instanceof Number n) {
                appender.append(n.intValue());
            } else {
                appender.append(value == null ? null : Integer.parseInt(value.toString()));
            }
        });
        
        register(Types.BIGINT, (appender, value) -> {
            if (value instanceof Number n) {
                appender.append(n.longValue());
            } else {
                appender.append(value == null ? null : Long.parseLong(value.toString()));
            }
        });
        
        register(Types.DOUBLE, (appender, value) -> {
            if (value instanceof Number n) {
                appender.append(n.doubleValue());
            } else {
                appender.append(value == null ? null : Double.parseDouble(value.toString()));
            }
        });
        
        register(Types.FLOAT, (appender, value) -> {
            if (value instanceof Number n) {
                appender.append(n.floatValue());
            } else {
                appender.append(value == null ? null : Float.parseFloat(value.toString()));
            }
        });

        register(Types.BOOLEAN, (appender, value) -> {
            if (value instanceof Boolean b) {
                appender.append(b);
            } else {
                appender.append(value == null ? null : Boolean.parseBoolean(value.toString()));
            }
        });

        register(Types.TIMESTAMP, (appender, value) -> {
            switch (value) {
                case null -> appender.append((LocalDateTime) null);
                case Timestamp ts -> appender.append(ts.toLocalDateTime());
                case LocalDateTime ldt -> appender.append(ldt);
                case Date date -> appender.append(DateUtils.toLocalDateTime(date));
                default -> {
                    String valStr = value.toString();
                    LocalDateTime parsed = null;

                    // 尝试默认格式 yyyy-MM-dd HH:mm:ss
                    try {
                        parsed = DateUtils.parseLocalDateTime(valStr);
                    } catch (Exception ignored) {
                        // ignore
                    }

                    if (parsed == null) {
                        // 尝试其他格式
                        String[] patterns = {
                                "yyyy/MM/dd HH:mm:ss",
                                DateUtils.NORM_DATE_PATTERN,
                                "yyyy-MM-dd'T'HH:mm:ss",
                                "yyyy-MM-dd'T'HH:mm:ss.SSS"
                        };
                        for (String pattern : patterns) {
                            try {
                                parsed = DateUtils.parseLocalDateTime(valStr, pattern);
                                break;
                            } catch (Exception ignored) {
                                // ignore
                            }
                        }
                    }

                    if (parsed != null) {
                        appender.append(parsed);
                    } else {
                        try {
                            appender.append(Timestamp.valueOf(valStr).toLocalDateTime());
                        } catch (Exception e) {
                            throw new SQLException("无法将值 '" + value + "' 解析为 TIMESTAMP", e);
                        }
                    }
                }
            }
        });
        
        register(Types.VARCHAR, (appender, value) -> appender.append(value == null ? null : value.toString()));
        register(Types.CHAR, (appender, value) -> appender.append(value == null ? null : value.toString()));
        register(Types.LONGVARCHAR, (appender, value) -> appender.append(value == null ? null : value.toString()));
    }

    public static void register(int sqlType, TypeHandler handler) {
        HANDLERS.put(sqlType, handler);
    }

    public static TypeHandler getHandler(int sqlType) {
        return HANDLERS.getOrDefault(sqlType, DEFAULT_HANDLER);
    }
}
