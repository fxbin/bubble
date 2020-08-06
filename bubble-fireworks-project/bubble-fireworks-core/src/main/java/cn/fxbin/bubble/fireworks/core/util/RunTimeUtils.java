package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.constant.StringPool;
import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;

/**
 * RunTimeUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 15:44
 */
@UtilityClass
public class RunTimeUtils {

    /**
     * 进程pid
     */
    private static volatile int pid = -1;

    /**
     * CPU 数量
     */
    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();


    /**
     * 获取当前进程的PID
     *
     * @return success: 进程ID, failure: -1
     */
    public static int getPid() {
        if(pid > 0 ) {
            return pid;
        }
        // something like '<pid>@<hostname>', at least in SUN/Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf(StringPool.AT);
        if (index > 0) {
            pid = NumberUtils.toInt(jvmName.substring(0, index), -1);
            return pid;
        }
        return pid;
    }


    /**
     * 返回系统启动到现在的时间
     *
     * @return {@code Duration}
     */
    public static Duration getUpTime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        return Duration.ofMillis(uptime);
    }


    /**
     * 返回输入的JVM的参数列表
     *
     * @return jvm 参数
     */
    public static String getJvmArguments() {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return StringUtils.join(inputArguments, StringPool.SPACE);
    }


    /**
     * 获取CPU核数
     *
     * @return cpu core number
     */
    public static int getCpuNum() {
        return CPU_NUM;
    }

}
