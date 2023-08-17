package cn.fxbin.bubble.core.util;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ExceptionUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 16:29
 */
@UtilityClass
public class ExceptionUtils {

    /**
     * getStackTrace
     *
     * @since 2020/3/23 16:32
     * @param throwable java.lang.Throwable
     * @return java.lang.String
     */
    public String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter(512);
        @Cleanup PrintWriter pw = new PrintWriter(stringWriter);
        throwable.printStackTrace(pw);
        return stringWriter.toString();
    }



}
