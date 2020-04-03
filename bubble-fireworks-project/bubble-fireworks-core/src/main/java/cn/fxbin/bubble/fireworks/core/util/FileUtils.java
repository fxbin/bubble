package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * FileUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 18:04
 */
@UtilityClass
public class FileUtils extends FileCopyUtils {

    /**
     * toFile MultipartFile 转成file
     *
     * @since 2020/3/23 18:06
     * @param multipartFile org.springframework.web.multipart.MultipartFile
     * @param file java.io.File
     */
    public void toFile(MultipartFile multipartFile, final File file) {
        try {
            FileUtils.toFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * toFile InputStream 转成file
     *
     * @since 2020/3/23 18:05
     * @param in java.io.InputStream
     * @param file java.io.File
     */
    public void toFile(InputStream in, final File file) {
        try (OutputStream out = new FileOutputStream(file)) {
            FileUtils.copy(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
