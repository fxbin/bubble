package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.constant.StringPool;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.RuntimeUtil;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * RunTimeUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/5 15:44
 */
@UtilityClass
public class RunTimeUtils extends RuntimeUtil {

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
    public int getPid() {
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
    public Duration getUpTime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        return Duration.ofMillis(uptime);
    }


    /**
     * 返回输入的JVM的参数列表
     *
     * @return jvm 参数
     */
    public String getJvmArguments() {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return StringUtils.join(inputArguments, StringPool.SPACE);
    }


    /**
     * 获取CPU核数
     *
     * @return cpu core number
     */
    public int getCpuNum() {
        return CPU_NUM;
    }

    /**
     * parseCmds
     *
     * @author fxbin
     * @since 2020/10/28 12:00
     * @param command (cmd|shell)命令
     * @return java.lang.String[]
     */
    public String[] parseCmds(String command) {
        return StringUtils.splitTrim(command, StringPool.SPACE);
    }

    /**
     * getOptionValue
     *
     * @author fxbin
     * @since 2020/10/28 13:43
     * @param option command option
     * @param command (cmd|shell)命令
     * @return java.lang.String
     */
    public String getOptionValue(String option, String command) {
        String[] args = parseCmds(command);
        String optionValue = null;
        for (int i = 0; i < args.length - 1; i++) {
            if (Objects.equals(args[i], option)) {
                optionValue = args[i + 1];
                break;
            }
        }
        return optionValue;
    }

    /**
     * exec
     *
     * @author fxbin
     * @since 2020/10/28 13:48
     * @param timeout 超时时间(秒)
     * @param cmds 命令列表，每个元素代表一条命令
     * @return java.lang.String
     */
    public String exec(int timeout, String... cmds) {

        int exitValue = -1;
        Process process = null;
        StringBuilder sbStd = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();

        long start = SystemClock.now()/1000;
        try {
            process = exec(cmds);
            @Cleanup BufferedReader brStd = new BufferedReader(new InputStreamReader(process.getInputStream()));
            @Cleanup BufferedReader brErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;

            while (true) {
                if (brStd.ready()) {
                    line = brStd.readLine();
                    sbStd.append(line).append("\n");
                    continue;
                }
                if (brErr.ready()) {
                    line = brErr.readLine();
                    sbErr.append(line).append("\n");
                    continue;
                }

                if (ObjectUtils.isNotEmpty(process)) {
                    try {
                        exitValue = process.exitValue();
                        break;
                    } catch (IllegalThreadStateException ignored) {
                    }
                }

                if (SystemClock.now() / 1000 - start > timeout) {
                    sbErr.append("\n命令执行超时退出.");
                    break;
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroy(process);
        }

        if (sbErr.length() > 0) {
            return sbErr.toString();
        }
        if (sbStd.length() > 0) {
            return sbStd.toString();
        }
        if (exitValue == 0) {
            return "success";
        } else {
            return "curl exit value(" + exitValue + ") is failed";
        }
    }

}
