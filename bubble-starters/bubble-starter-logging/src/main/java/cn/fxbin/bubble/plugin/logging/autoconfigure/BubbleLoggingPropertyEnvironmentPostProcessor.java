package cn.fxbin.bubble.plugin.logging.autoconfigure;

import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.plugin.logging.util.LogFactory;
import cn.hutool.system.SystemUtil;
import com.alipay.common.tracer.core.generator.TraceIdGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.auto.annotation.AutoEnvPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * BubbleLoggingPropertyEnvironmentPostProcessor
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/5/16 0:33
 */
@Slf4j
@AutoEnvPostProcessor
public class BubbleLoggingPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * The name of the Spring properøty that contains the name of the log file. Names can
     * be an exact location or relative to the current directory.
     * @since 2.2.0
     */
    public static final String FILE_NAME_PROPERTY = "logging.file.name";

    /**
     * The name of the Spring property that contains the directory where log files are
     * written.
     * @since 2.2.0
     */
    public static final String FILE_PATH_PROPERTY = "logging.file.path";

    /**
     * The name of the Spring properøty that contains the name of the log file. Names can
     * be an exact location or relative to the current directory.
     *
     * >>> before springboot 2.2.0
     */
    public static final String FILE_PROPERTY = "logging.file";

    /**
     * The name of the Spring property that contains the directory where log files are
     * written.
     *
     * >>> before springboot 2.2.0
     */
    public static final String PATH_PROPERTY = "logging.path";

    /**
     * The name of the Spring property that contains a reference to the logging
     * configuration to load.
     */
    public static final String CONFIG_PROPERTY = "logging.config";

    /**
     * The "active profiles" property name.
     */
    public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";

    /**
     * The name of the SpringBoot Application Name
     */
    private static final String APPLICATION_NAME = "spring.application.name";

    /**
     * The log file suffix
     */
    private static final String LOG_SUFFIX = ".log";

    /**
     * line separator
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * 默认环境
     */
    public static final List<String> PROFILES = Lists.newArrayList("default", "dev", "local");

    public static final Set<String> ENDPOINTS = Sets.newHashSet("health", "info", "loggers");

    /**
     * spring-boot 2.2.0 版本，切分记录为 22
     */
    public static final Integer SPRING_BOOT_MAIN_VERSION = 22;

    public static final String ENDPOINTS_WEB_INCLUDE = "management.endpoints.web.exposure.include";

    public static final String TLOG_LOG4J2_INHERITABLE = "log4j2.isThreadContextMapInheritable";

    public static final String TLOG_ID_GENERATOR = "tlog.id-generator";

    /**
     * rocketmq客户端日志使用slf4j
     * 参照 {@link org.apache.rocketmq.client.log.ClientLogger}
     */
    public static final String ROCKETMQ_CLIENT_LOG_USESLF4J = "rocketmq.client.logUseSlf4j";

    /**
     * Post-process the given {@code environment}.
     *
     * <p>
     *     logging.file.name 属性 参见 {@link org.springframework.boot.logging.LogFile#applyTo(java.util.Properties)}
     *     因此，真实路径需要 拼接 logging.file.path 属性，否则仅为 文件名
     * </p>
     *
     * @param environment the environment to post-process
     * @param application the application to which the environment belongs
     * @see org.springframework.boot.logging.LogFile
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        // 鉴于 SpringBoot 2.2.0 之后 日志属性为： `logging.file.name` & `logging.file.path`
        // SpringBoot 2.2.0 之前 日志属性为： `logging.file` & `logging.path` , 需向下兼容
        String currentVersion = SpringBootVersion.getVersion();
        String mainVersion = currentVersion.substring(0, 3).replaceAll("\\.", "").trim();
        System.out.println("--- MainVersion--->>>" + mainVersion);

        // 如果小于 2.2.0 版本，赋予旧版属性值
        if (SPRING_BOOT_MAIN_VERSION > Integer.parseInt(mainVersion)) {
            initializeEarlyLoggingProperties(PATH_PROPERTY, FILE_PROPERTY, environment);
        } else {
            initializeEarlyLoggingProperties(FILE_PATH_PROPERTY, FILE_NAME_PROPERTY, environment);
        }

        // 追加 loggers
        applyWebEndpointsOfLoggers(environment);
        // 配置 tlog 异步日志 属性
        this.applyTLogDefaultConfig();
        // 配置 rocketmq client 日志属性
        LogFactory.applyTo(ROCKETMQ_CLIENT_LOG_USESLF4J, Boolean.TRUE.toString());

        // 配置 sofatracer 属性
        this.applySofaTracerDefaultConfig();
    }

    /**
     * 应用 sofatracer 默认配置
     */
    private void applySofaTracerDefaultConfig() {
        LogFactory.applyTo("com.alipay.sofa.tracer.enable", Boolean.TRUE.toString());
    }

    /**
     * 应用tlog默认配置
     */
    private void applyTLogDefaultConfig() {
        LogFactory.applyTo(TLOG_LOG4J2_INHERITABLE, Boolean.TRUE.toString());
        LogFactory.applyTo(TLOG_ID_GENERATOR, TraceIdGenerator.class.getName());
    }

    /**
     * applyWebEndpointsOfLoggers
     *
     * @since 2021/9/9 16:36
     * @param environment {@link ConfigurableEnvironment}
     */
    private void applyWebEndpointsOfLoggers(ConfigurableEnvironment environment) {
        String endpoints = environment.getProperty(ENDPOINTS_WEB_INCLUDE);
        if (StringUtils.isBlank(endpoints)) {
            endpoints = StringUtils.join(ENDPOINTS);
        }
        if (!endpoints.contains("loggers") && !endpoints.contains("*")) {
            endpoints = StringUtils.wrapIfMissing(endpoints, "", ",");
            endpoints = StringUtils.concat(true, endpoints, "loggers");
        }
        System.out.format("\33[31;4m%s:%s%s\33[0m%n", ENDPOINTS_WEB_INCLUDE, endpoints, LINE_SEPARATOR);
        LogFactory.applyTo(ENDPOINTS_WEB_INCLUDE, endpoints);
    }

    /**
     * initializeEarlyLoggingProperties
     *
     * @since 2021/9/6 16:38
     * @param logPath 日志路径
     * @param logName 日志文件名
     * @param environment {@link ConfigurableEnvironment}
     */
    private void initializeEarlyLoggingProperties(String logPath, String logName,
                                                  ConfigurableEnvironment environment) {
        String activeProfiles = environment.getProperty(ACTIVE_PROFILES_PROPERTY);
        String appName = Optional.ofNullable(environment.getProperty(APPLICATION_NAME)).orElse("default");
        String applicationFilename = appName + LOG_SUFFIX;
        String logNameValue = environment.getProperty(logName, applicationFilename);

        Map<String, Object> map = Maps.newHashMap();
        map.put(APPLICATION_NAME, appName);
        // 是否设定默认值
        // 1. 如果是在mac系统（max os x），默认值
        // 2. 1). 不满足的话，判断开发环境, 本地环境以及默认情况;
        boolean isDefault = SystemUtil.getOsInfo().isMacOsX();
        if (!isDefault) {
            isDefault = PROFILES.contains(activeProfiles);
        }
        if (isDefault) {
            map.put(logPath, this.getLogBaseDir(appName));
            map.put(logName, logNameValue);
            map.put(CONFIG_PROPERTY, LogFactory.getDefaultLogConfig());
        } else {
            map.put(logPath, this.getDefaultLogDir(appName));
            map.put(logName, LogFactory.getLogName());
            map.put(CONFIG_PROPERTY, LogFactory.getLogConfig());
        }

        // 替换部分默认默认属性
        replaceSystemProperties(logPath, logName, environment, map);
    }

    public String getDefaultLogDir(String appName) {
        return LogFactory.getDefaultLogDir() + appName + File.separator;
    }

    public String getLogBaseDir(String appName) {
        return LogFactory.getLogBaseDir() + appName + File.separator;
    }



    /**
     * replaceSystemProperties
     *
     * @since 2021/9/8 17:35
     * @param logPath 日志文件路径
     * @param logName 日志文件名
     * @param environment {@link ConfigurableEnvironment}
     * @param map property source
     * @see org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor
     */
    private void replaceSystemProperties(String logPath, String logName,
                                         ConfigurableEnvironment environment,
                                         Map<String, Object> map) {
        String logPathValue= environment.getProperty(logPath, (String) map.get(logPath));
        String logNameValue = environment.getProperty(logName, (String) map.get(logName));
        String logConfigValue = environment.getProperty(CONFIG_PROPERTY, (String) map.get(CONFIG_PROPERTY));


        map.put(logPath, logPathValue);
        map.put(logName, logNameValue);
        if (StringUtils.isNotBlank(logConfigValue)) {
            map.put(CONFIG_PROPERTY, logConfigValue);
        }

        // 打印 日志相关参数值
        System.out.format("\33[31;4m%s\33[0m%n",buildConsoleText(map.get(logPath), map.get(logName), map.get(CONFIG_PROPERTY)));

        // 设置系统变量
        map.keySet().forEach(key -> LogFactory.applyTo(key, String.valueOf(map.get(key))));
    }

    /**
     * buildConsoleText
     *
     * @since 2021/9/3 11:14
     * @param path 文件路径
     * @param name 文件名
     * @return java.lang.String
     */
    String buildConsoleText(Object path, Object name, Object config) {
        return LINE_SEPARATOR +
                " :: INFO App log base directory is: " + path +
                LINE_SEPARATOR +
                " :: INFO App log output is: " + name +
                LINE_SEPARATOR +
                " :: INFO App log config is: " + config +
                LINE_SEPARATOR;
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}