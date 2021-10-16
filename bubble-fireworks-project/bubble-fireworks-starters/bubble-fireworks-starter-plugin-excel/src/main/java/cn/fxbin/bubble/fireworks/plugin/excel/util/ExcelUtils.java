package cn.fxbin.bubble.fireworks.plugin.excel.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.Charsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletResponse;


/**
 * ExcelUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 14:48
 */
@UtilityClass
public class ExcelUtils {



    /**
     * setResponse
     *
     * @since 2020/4/7 16:13
     * @param response javax.servlet.http.HttpServletResponse
     * @param fileName excel file name
     */
    public void setResponse(HttpServletResponse response, String fileName) {

        String encodeFileName = UriUtils.encode(fileName, Charsets.UTF_8);
        // 兼容各种浏览器下载：
        // https://blog.robotshell.org/2012/deal-with-http-header-encoding-for-file-download/
        String disposition = "attachment;" +
                "filename=\"" + encodeFileName + "\";" +
                "filename*=utf-8''" + encodeFileName;

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition);
    }

}
