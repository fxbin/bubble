package cn.fxbin.bubble.data.mybatisplus.util;

import cn.fxbin.bubble.core.exception.UtilException;
import cn.hutool.core.thread.ThreadUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * JdbcUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/8/18 16:41
 */
@Slf4j
@UtilityClass
public class JdbcUtils extends com.baomidou.mybatisplus.extension.toolkit.JdbcUtils {

    /**
     * 验证
     *
     * @param url      url
     * @param username 用户名
     * @param password 密码
     * @return {@link Boolean}
     */
    public Boolean validate(String url, String username, String password) {
        Future<Boolean> future = ThreadUtil.execAsync(() -> checkConnection(url, username, password));
        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new UtilException("连接超时，校验失败");
        }
    }

    /**
     * 检查连接
     *
     * @param url      url
     * @param username 用户名
     * @param password 密码
     * @return {@link Boolean}
     */
    private Boolean checkConnection(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
        } catch (SQLException e) {
            log.error("连接异常", e);
            throw new UtilException(e.getMessage());
        }
        return Boolean.TRUE;
    }

}
