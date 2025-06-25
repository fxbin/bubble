package cn.fxbin.bubble.plugin.logging.util;

import cn.hutool.core.text.StrBuilder;
import cn.fxbin.bubble.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.auto.annotation.AutoIgnore;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * LogFactory
 *
 * <p>
 * 日志工厂类，负责日志系统的初始化和配置管理。
 * 提供日志目录、日志文件名、日志配置文件等核心配置的统一管理。
 * 支持动态获取应用名称，并提供日志路径的标准化处理功能。
 * </p>
 *
 * <p>
 * 主要功能：
 * - 日志配置的统一管理和初始化
 * - 提供标准的日志目录和文件命名规范
 * - 支持多环境的日志配置切换
 * - 提供路径处理的工具方法
 * </p>
 *
 * <p>
 * 配置说明：
 * - 默认日志目录：用户主目录/logs/
 * - 默认日志文件：app.log
 * - 生产环境配置：classpath:log4j2-file.xml
 * - 开发环境配置：classpath:log4j2-local.xml
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/24 17:27
 */
@Slf4j
@AutoIgnore
@Component
public class LogFactory implements EnvironmentAware, InitializingBean {

    /**
     * 默认日志目录名称
     */
    public static final String LOG_DIR = "logs";
    
    /**
     * 默认应用日志文件名
     */
    public static final String LOG_FILE_APP = "app.log";
    
    /**
     * 生产环境日志配置文件路径
     */
    public static final String LOG_CONFIG = "classpath:log4j2-file.xml";
    
    /**
     * 开发环境默认日志配置文件路径
     */
    public static final String DEFAULT_LOG_CONFIG = "classpath:log4j2-local.xml";
    
    /**
     * 用户主目录系统属性键
     */
    public static final String USER_HOME = "user.home";

    /**
     * Spring Boot应用名称配置键
     * 用于获取spring.application.name配置值
     */
    private static final String APPLICATION_NAME = "spring.application.name";

    /**
     * Spring环境上下文
     * 通过EnvironmentAware接口注入，用于获取配置属性
     * 受"spring.profiles.active"配置属性影响
     */
    @Nullable
    private Environment environment;

    /**
     * 服务名称
     * 从spring.application.name配置中获取
     */
    private String serviceId;

    /**
     * 日志基础目录路径
     */
    private static String logBaseDir;
    
    /**
     * 日志文件名称
     */
    private static String logName;
    
    /**
     * 日志配置文件路径
     */
    private static String logConfig;

    static {
        try {
            initializeDefault();
        } catch (Exception e) {
            log.error("FATAL ERROR when initializing logging config", e);
        }
    }

    /**
     * 初始化默认日志配置
     * 
     * <p>
     * 设置默认的日志基础目录、日志文件名和配置文件路径。
     * 日志基础目录默认为用户主目录下的logs文件夹。
     * </p>
     */
    private static void initializeDefault() {
        logBaseDir = addSeparator(System.getProperty(USER_HOME)) + LOG_DIR + File.separator;
        logName = LOG_FILE_APP;
        logConfig = LOG_CONFIG;
    }

    /**
     * 获取日志基础目录路径
     * 
     * @return 日志基础目录的完整路径
     */
    public static String getLogBaseDir() {
        return logBaseDir;
    }

    /**
     * 获取日志文件名称
     * 
     * @return 日志文件名
     */
    public static String getLogName() {
        return logName;
    }

    /**
     * 获取日志配置文件路径
     * 
     * @return 日志配置文件路径
     */
    public static String getLogConfig() {
        return logConfig;
    }

    /**
     * 获取默认日志目录
     * 
     * @return 默认日志目录路径（相对路径）
     */
    public static String getDefaultLogDir() {
        return File.separator + LOG_DIR + File.separator;
    }

    /**
     * 获取默认日志配置文件路径
     * 
     * @return 默认日志配置文件路径
     */
    public static String getDefaultLogConfig() {
        return DEFAULT_LOG_CONFIG;
    }

    /**
     * 获取服务ID（应用名称）
     * 
     * @return 服务名称，来源于spring.application.name配置
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * 为目录路径添加分隔符
     * 
     * <p>
     * 确保目录路径以文件分隔符结尾，便于后续路径拼接操作。
     * 如果路径已经以分隔符结尾，则不进行任何操作。
     * </p>
     * 
     * @param dir 目录路径
     * @return 以分隔符结尾的目录路径
     */
    public static String addSeparator(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        return dir;
    }

    /**
     * 移除路径开头的分隔符
     * 
     * <p>
     * 从路径字符串中提取最后一个路径组件，移除前导的文件分隔符。
     * 主要用于获取文件名或目录名的纯净形式。
     * </p>
     * 
     * @param dir 包含分隔符的路径
     * @return 移除前导分隔符后的路径组件
     */
    public static String subSeparator(String dir) {
        if (dir.startsWith(File.separator)) {
            String[] dirs = dir.split(Matcher.quoteReplacement(File.separator));
            dir = dirs[dirs.length-1];
        }
        return dir;
    }

    /**
     * 连接多个路径组件并处理最后一个组件的分隔符
     * 
     * <p>
     * 将多个路径组件连接成完整路径，前面的组件会添加分隔符，
     * 最后一个组件会移除前导分隔符。适用于构建复杂的文件路径。
     * </p>
     * 
     * @param args 路径组件数组
     * @return 连接后的完整路径
     */
    public static String concatLastSubSeparator(String... args) {
        final StrBuilder sb = new StrBuilder();
        for (int i = 0; i < args.length-1; i++) {
            sb.append(addSeparator(args[i]));
        }
        sb.append(subSeparator(args[args.length-1]));
        return sb.toString();
    }

    /**
     * 应用系统属性配置
     * 
     * <p>
     * 将指定的键值对设置到系统属性中，用于日志系统的配置。
     * 只有当值不为空时才会进行设置操作。
     * </p>
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public static void applyTo(String key, String value) {
        put(System.getProperties(), key, value);
        put(System.getProperties(), key, value);
    }

    /**
     * 向Properties对象中添加键值对
     * 
     * <p>
     * 内部工具方法，用于安全地向Properties对象添加配置项。
     * 只有当值不为空白时才会执行添加操作。
     * </p>
     * 
     * @param properties Properties对象
     * @param key 属性键
     * @param value 属性值
     */
    private static void put(Properties properties, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            properties.put(key, value);
        }
    }

    /**
     * Bean属性设置完成后的初始化回调
     * 
     * <p>
     * 在所有Bean属性设置完成后执行，用于获取和验证应用名称配置。
     * 确保spring.application.name配置项已正确设置。
     * </p>
     * 
     * @throws Exception 当配置验证失败或初始化过程中发生错误时抛出
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.serviceId = environment.getProperty(APPLICATION_NAME);
        Assert.notNull(this.serviceId,
                "Please add the 【spring.application.name】 configuration in the application.yml or application.properties");
    }

    /**
     * 设置运行环境
     * 
     * <p>
     * 通过EnvironmentAware接口注入Spring环境上下文，
     * 用于后续获取配置属性和环境信息。
     * </p>
     * 
     * @param environment Spring环境上下文对象
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}