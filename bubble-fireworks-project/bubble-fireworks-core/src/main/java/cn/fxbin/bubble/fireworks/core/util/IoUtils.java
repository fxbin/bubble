package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.constant.StringPool;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * IoUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 16:52
 */
@Slf4j
@UtilityClass
public class IoUtils extends StreamUtils {

    /**
     * Writes chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @since 2020/6/12 18:38
     * @param data the <code>String</code> to write, null ignored
     * @param output the <code>OutputStream</code> to write to
     * @param encoding the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     */
    public void write(@Nullable final String data, final OutputStream output, final Charset encoding) throws IOException {
        if (data != null) {
            output.write(data.getBytes(encoding));
        }
    }

    /**
     * readerFileAsString
     *
     * @since 2020/6/15 10:38
     * @param filename the system-dependent filename
     * @return java.lang.String
     */
    public String readerFileAsString(String filename) {
        return readerFileAsString(new File(filename));
    }


    /**
     * readerFileAsString
     *
     * @since 2020/6/15 15:56
     * @param inputStream InputStream
     * @return java.lang.String
     */
    public String readerFileAsString(InputStream inputStream) {
        File file = new File(StringUtils.getUUID());
        FileUtils.toFile(inputStream, file);
        return readerFileAsString(file);
    }

    /**
     * readerToFile
     *
     * @since 2020/12/1 15:50
     * @param inputStream InputStream
     * @return java.io.File
     */
    public File readerToFile(InputStream inputStream) {
        File file = new File(StringUtils.getUUID());
        FileUtils.toFile(inputStream, file);
        return file;
    }

    /**
     * readerFileAsString
     *
     * @since 2020/6/15 10:23
     * @param file the file object
     * @return java.lang.String
     */
    public String readerFileAsString(File file) {
        try(
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                FileChannel fileChannel = raf.getChannel();
                ByteArrayOutputStream out = new ByteArrayOutputStream();

        ){
            int bufferSize = 1024;
            if (bufferSize > fileChannel.size()) {
                bufferSize = (int) fileChannel.size();
            }
            ByteBuffer buff = ByteBuffer.allocate(bufferSize);
            while (fileChannel.read(buff) > 0) {
                out.write(buff.array(), 0, buff.position());
                buff.clear();
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("readerFileAsString error", e);
        }
        return StringPool.EMPTY;
    }

}
