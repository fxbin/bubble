package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
     * Writes a String to a file creating the file if it does not exist.
     *
     * @since 2020/6/12 18:38
     * @param file the file to write
     * @param data the content to write to the file
     */
    public void writeToFile(final File file, final String data) {
        writeToFile(file, data, StandardCharsets.UTF_8, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @since 2020/6/12 18:38
     * @param file the file to write
     * @param data the content to write to the file
     * @param append if {@code true}, then the String will be added to the
     *               end of the file rather than overwriting
     */
    public void writeToFile(final File file, final String data, final boolean append){
        writeToFile(file, data, StandardCharsets.UTF_8, append);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @since 2020/6/12 18:38
     * @param file the file to write
     * @param data the content to write to the file
     * @param encoding the encoding to use, {@code null} means platform default
     */
    public void writeToFile(final File file, final String data, final Charset encoding) {
        writeToFile(file, data, encoding, false);
    }


    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @since 2020/6/12 18:37
     * @param file the file to write
     * @param data the content to write to the file
     * @param encoding the encoding to use, {@code null} means platform default
     * @param append if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     */
    public void writeToFile(final File file, final String data, final Charset encoding, final boolean append) {
        try (OutputStream out = new FileOutputStream(file, append)) {
            IoUtils.write(data, out, encoding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
