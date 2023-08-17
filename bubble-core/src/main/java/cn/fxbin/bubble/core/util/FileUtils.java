package cn.fxbin.bubble.core.util;

import cn.hutool.core.io.FileUtil;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * FileUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 18:04
 */
@UtilityClass
public class FileUtils extends FileUtil {

    /**
     * The default buffer size used when copying bytes.
     */
    public final int BUFFER_SIZE = StreamUtils.BUFFER_SIZE;

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
            copy(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //---------------------------------------------------------------------
    // Copy methods for java.io.File
    //---------------------------------------------------------------------

    /**
     * Copy the contents of the given input File to the given output File.
     * @param in the file to copy from
     * @param out the file to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public int copy(File in, File out) throws IOException {
        Assert.notNull(in, "No input File specified");
        Assert.notNull(out, "No output File specified");
        return copy(Files.newInputStream(in.toPath()), Files.newOutputStream(out.toPath()));
    }

    /**
     * Copy the contents of the given byte array to the given output File.
     * @param in the byte array to copy from
     * @param out the file to copy to
     * @throws IOException in case of I/O errors
     */
    public void copy(byte[] in, File out) throws IOException {
        Assert.notNull(in, "No input byte array specified");
        Assert.notNull(out, "No output File specified");
        copy(new ByteArrayInputStream(in), Files.newOutputStream(out.toPath()));
    }

    /**
     * Copy the contents of the given input File into a new byte array.
     * @param in the file to copy from
     * @return the new byte array that has been copied to
     * @throws IOException in case of I/O errors
     */
    public byte[] copyToByteArray(File in) throws IOException {
        Assert.notNull(in, "No input File specified");
        return copyToByteArray(Files.newInputStream(in.toPath()));
    }


    //---------------------------------------------------------------------
    // Copy methods for java.io.InputStream / java.io.OutputStream
    //---------------------------------------------------------------------

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Closes both streams when done.
     * @param in the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public int copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");

        try {
            return StreamUtils.copy(in, out);
        }
        finally {
            close(in);
            close(out);
        }
    }

    /**
     * Copy the contents of the given byte array to the given OutputStream.
     * Closes the stream when done.
     * @param in the byte array to copy from
     * @param out the OutputStream to copy to
     * @throws IOException in case of I/O errors
     */
    public void copy(byte[] in, OutputStream out) throws IOException {
        Assert.notNull(in, "No input byte array specified");
        Assert.notNull(out, "No OutputStream specified");

        try {
            out.write(in);
        }
        finally {
            close(out);
        }
    }

    /**
     * Copy the contents of the given InputStream into a new byte array.
     * Closes the stream when done.
     * @param in the stream to copy from (may be {@code null} or empty)
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public byte[] copyToByteArray(@Nullable InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }


    //---------------------------------------------------------------------
    // Copy methods for java.io.Reader / java.io.Writer
    //---------------------------------------------------------------------

    /**
     * Copy the contents of the given Reader to the given Writer.
     * Closes both when done.
     * @param in the Reader to copy from
     * @param out the Writer to copy to
     * @return the number of characters copied
     * @throws IOException in case of I/O errors
     */
    public int copy(Reader in, Writer out) throws IOException {
        Assert.notNull(in, "No Reader specified");
        Assert.notNull(out, "No Writer specified");

        try {
            int charCount = 0;
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, charsRead);
                charCount += charsRead;
            }
            out.flush();
            return charCount;
        }
        finally {
            close(in);
            close(out);
        }
    }

    /**
     * Copy the contents of the given String to the given Writer.
     * Closes the writer when done.
     * @param in the String to copy from
     * @param out the Writer to copy to
     * @throws IOException in case of I/O errors
     */
    public void copy(String in, Writer out) throws IOException {
        Assert.notNull(in, "No input String specified");
        Assert.notNull(out, "No Writer specified");

        try {
            out.write(in);
        }
        finally {
            close(out);
        }
    }

    /**
     * Copy the contents of the given Reader into a String.
     * Closes the reader when done.
     * @param in the reader to copy from (may be {@code null} or empty)
     * @return the String that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public String copyToString(@Nullable Reader in) throws IOException {
        if (in == null) {
            return "";
        }

        StringWriter out = new StringWriter(BUFFER_SIZE);
        copy(in, out);
        return out.toString();
    }

    /**
     * Attempt to close the supplied {@link Closeable}, silently swallowing any
     * exceptions.
     * @param closeable the {@code Closeable} to close
     */
    private void close(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException ex) {
            // ignore
        }
    }

}
