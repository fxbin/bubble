package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.constant.CharPool;
import cn.fxbin.bubble.fireworks.core.constant.StringPool;
import cn.fxbin.bubble.fireworks.core.exception.UtilException;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.web.util.HtmlUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * StringUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 17:10
 */
@UtilityClass
public class StringUtils extends org.springframework.util.StringUtils {

    /**
     * 验证字符串是否是数据库字段
     */
    private final Pattern PROPERTY_IS_COLUMN = Pattern.compile("^\\w\\S*[\\w\\d]*$");


    /**
     * Check whether the given {@code String} is not empty.
     * <p>This method accepts any Object as an argument, comparing it to
     * {@code null} and the empty String. As a consequence, this method
     * will never return {@code true} for a non-null non-String object.
     * <p>The Object signature is useful for general attribute handling code
     * that commonly deals with Strings but generally has to iterate over
     * Objects since attributes may e.g. be primitive value objects as well.
     * @param str the candidate String
     * @since 3.2.1
     */
    public boolean isNotEmpty(@Nullable Object str) {
        return !(str == null || "".equals(str));
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.
     * <pre class="code">
     * StringUtil.isBlank(null) = true
     * StringUtil.isBlank("") = true
     * StringUtil.isBlank(" ") = true
     * StringUtil.isBlank("12345") = false
     * StringUtil.isBlank(" 12345 ") = false
     * </pre>
     *
     * @param cs the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public boolean isBlank(@Nullable final CharSequence cs) {
        return !StringUtils.hasText(cs);
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     * <pre>
     * StringUtil.isNotBlank(null)	  = false
     * StringUtil.isNotBlank("")		= false
     * StringUtil.isNotBlank(" ")	   = false
     * StringUtil.isNotBlank("bob")	 = true
     * StringUtil.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     * not empty and not null and not whitespace
     * @see Character#isWhitespace
     */
    public boolean isNotBlank(@Nullable final CharSequence cs) {
        return StringUtils.hasText(cs);
    }

    /**
     * isAnyBlank 有 任意 一个 Blank
     *
     * @since 2020/3/23 17:23
     * @param css java.lang.CharSequence
     * @return boolean
     */
    public boolean isAnyBlank(final CharSequence... css) {
        if (ObjectUtils.isEmpty(css)) {
            return true;
        }
        return Stream.of(css).anyMatch(StringUtils::isBlank);
    }

    /**
     * isNoneBlank 是否全非 Blank
     *
     * @since 2020/3/23 17:24
     * @param css java.lang.CharSequence
     * @return boolean
     */
    public boolean isNoneBlank(final CharSequence... css) {
        if (ObjectUtils.isEmpty(css)) {
            return false;
        }
        return Stream.of(css).allMatch(StringUtils::isNotBlank);
    }

    /**
     * isNumeric 判断一个字符串是否是数字
     *
     * @since 2020/3/23 17:24
     * @param cs the CharSequence to check, may be null
     * @return boolean
     */
    public boolean isNumeric(final CharSequence cs) {
        if (StringUtils.isBlank(cs)) {
            return false;
        }
        for ( int i = cs.length(); --i >= 0; ) {
            int chr = cs.charAt(i);
            if (chr < 48 || chr > 57) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll the {@code Collection} to convert
     * @return the delimited {@code String}
     */
    public String join(Collection<?> coll) {
        return StringUtils.collectionToCommaDelimitedString(coll);
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll  the {@code Collection} to convert
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public String join(Collection<?> coll, String delim) {
        return StringUtils.collectionToDelimitedString(coll, delim);
    }

    /**
     * Convert a {@code String} array into a comma delimited {@code String}
     * (i.e., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr the array to display
     * @return the delimited {@code String}
     */
    public String join(Object[] arr) {
        return StringUtils.arrayToCommaDelimitedString(arr);
    }

    /**
     * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr   the array to display
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public String join(Object[] arr, String delim) {
        return StringUtils.arrayToDelimitedString(arr, delim);
    }

    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     *
     * <p>A negative start position can be used to start/end {@code n}
     * characters from the end of the String.</p>
     *
     * <p>The returned substring starts with the character in the {@code start}
     * position and ends before the {@code end} position. All position counting is
     * zero-based -- i.e., to start at the beginning of the string use
     * {@code start = 0}. Negative start and end positions can be used to
     * specify offsets relative to the end of the String.</p>
     *
     * <p>If {@code start} is not strictly to the left of {@code end}, ""
     * is returned.</p>
     *
     * <pre>
     * StringUtils.substring(null, *, *)    = null
     * StringUtils.substring("", * ,  *)    = "";
     * StringUtils.substring("abc", 0, 2)   = "ab"
     * StringUtils.substring("abc", 2, 0)   = ""
     * StringUtils.substring("abc", 2, 4)   = "c"
     * StringUtils.substring("abc", 4, 6)   = ""
     * StringUtils.substring("abc", 2, 2)   = ""
     * StringUtils.substring("abc", -2, -1) = "b"
     * StringUtils.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str  the String to get the substring from, may be null
     * @param start  the position to start from, negative means
     *  count back from the end of the String by this many characters
     * @param end  the position to end at (exclusive), negative means
     *  count back from the end of the String by this many characters
     * @return substring from start position to end position,
     *  {@code null} if null String input
     */
    public String substring(final String str, int start, int end) {
        if (isBlank(str)) {
            return StringPool.EMPTY;
        }

        // handle negatives
        if (end < 0) {
            // remember end is negative
            end = str.length() + end;
        }
        if (start < 0) {
            // remember start is negative
            start = str.length() + start;
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return StringPool.EMPTY;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * splitTrim 分割 字符串 删除常见 空白符
     *
     * @since 2020/3/23 17:29
     * @param str 字符串
     * @param delimiter 分割符
     * @return java.lang.String[]
     */
    public String[] splitTrim(@Nullable String str, @Nullable String delimiter) {
        return delimitedListToStringArray(str, delimiter, " \t\n\n\f");
    }

    /**
     * escapeHtml 转义HTML用于安全过滤
     *
     * @since 2020/3/23 17:28
     * @param html html string
     * @return java.lang.String
     */
    public String escapeHtml(String html) {
        return HtmlUtils.htmlEscape(html);
    }

    /**
     * format
     *
     * <p>
     * 格式化文本, {} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") =》 this is \{} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") =》 this is \a for b<br>
     * </p>
     *
     * @since 2020/3/23 17:29
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params 参数值
     * @return java.lang.String
     */
    public String format(CharSequence template, Object... params) {
        if (null == template) {
            return null;
        }
        if (ArrayUtils.isEmpty(params) || isBlank(template)) {
            return template.toString();
        }
        return format(template.toString(), params);
    }

    /**
     * format
     *
     * <p>
     * 格式化字符串<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 		通常使用：format("this is {} for {}", "a", "b") =》 this is a for b<br>
     * 		转义{}： 	format("this is \\{} for {}", "a", "b") =》 this is \{} for a<br>
     * 		转义\：		format("this is \\\\{} for {}", "a", "b") =》 this is \a for b<br>
     * </p>
     *
     * @since 2020/3/23 17:59
     * @param strPattern 字符串模板
     * @param argArray 参数列表
     * @return java.lang.String
     */
    public String format(final String strPattern, final Object... argArray) {
        if (StringUtils.isBlank(strPattern) || ArrayUtils.isEmpty(argArray)) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();

        //初始化定义好的长度以获得更好的性能
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        //记录已经处理到的位置
        int handledPosition = 0;
        //占位符所在位置
        int delimIndex;
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(StringPool.EMPTY_JSON, handledPosition);
            //剩余部分无占位符
            if (delimIndex == -1) {
                //不带占位符的模板直接返回
                if (handledPosition == 0) {
                    return strPattern;
                } else { //字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                //转义符
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == CharPool.BACK_SLASH) {
                    //双转义符
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == CharPool.BACK_SLASH) {
                        //转义符之前还有一个转义符，占位符依旧有效
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(StringUtils.utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        //占位符被转义
                        argIndex--;
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(CharPool.LEFT_BRACE);
                        handledPosition = delimIndex + 1;
                    }
                } else {//正常占位符
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(StringUtils.utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        //加入最后一个占位符后所有的字符
        sbuf.append(strPattern, handledPosition, strPattern.length());

        return sbuf.toString();
    }

    /**
     * utf8Str
     *
     * 将对象转为字符串<br>
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @since 2020/3/23 17:58
     * @param obj 对象
     * @return java.lang.String
     */
    public String utf8Str(Object obj) {
        return str(obj, CharsetUtils.CHARSET_UTF_8);
    }

    /**
     * str
     *
     * 将对象转为字符串<br>
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @since 2020/3/23 17:58
     * @param obj 对象
     * @param charsetName 字符集
     * @return java.lang.String
     */
    public String str(Object obj, String charsetName) {
        return str(obj, Charset.forName(charsetName));
    }

    /**
     * str
     *
     * <p>
     * 将对象转为字符串<br>
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     * </p>
     *
     * @since 2020/3/23 17:57
     * @param obj 对象
     * @param charset 字符集
     * @return java.lang.String
     */
    public String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return str((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            return str((Byte[]) obj, charset);
        } else if (obj instanceof ByteBuffer) {
            return str((ByteBuffer) obj, charset);
        } else if (ArrayUtils.isArray(obj)) {
            return ArrayUtils.toString(obj);
        }
        return obj.toString();
    }

    /**
     * Blob 转为 String 格式
     *
     * @param blob Blob 对象
     * @return 转换后的
     */
    public String blob2String(Blob blob) {
        if (null != blob) {
            try {
                byte[] returnValue = blob.getBytes(1, (int) blob.length());
                return utf8Str(returnValue);
            } catch (Exception e) {
                throw new UtilException("Blob Convert To String Error!");
            }
        }
        return null;
    }

    /**
     * 大写第一个字母
     *
     * @param src 源字符串
     * @return 返回第一个大写后的字符串
     */
    public String upperFirst(String src) {
        if (Character.isLowerCase(src.charAt(0))) {
            return 1 == src.length() ? src.toUpperCase() : Character.toUpperCase(src.charAt(0)) + src.substring(1);
        }
        return src;
    }

    /**
     * 判断字符串是不是驼峰命名
     *
     * <li> 包含 '_' 不算 </li>
     * <li> 首字母大写的不算 </li>
     *
     * @param str 字符串
     * @return 结果
     */
    public boolean isCamel(String str) {
        if (str.contains(StringPool.UNDERLINE)) {
            return false;
        }
        return Character.isLowerCase(str.charAt(0));
    }

    /**
     * 判断字符串是否符合数据库字段的命名
     *
     * @param str 字符串
     * @return 判断结果
     */
    public boolean isNotColumnName(String str) {
        return !PROPERTY_IS_COLUMN.matcher(str).matches();
    }

    /**
     * 字符串驼峰转下划线格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public String camelToUnderline(String param) {
        if (isBlank(param)) {
            return StringPool.EMPTY;
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append(StringPool.UNDERLINE);
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 字符串下划线转驼峰格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public String underlineToCamel(String param) {
        if (isBlank(param)) {
            return StringPool.EMPTY;
        }
        String temp = param.toLowerCase();
        int len = temp.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = temp.charAt(i);
            if (c == CharPool.UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(temp.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public String firstToLowerCase(String param) {
        if (isBlank(param)) {
            return StringPool.EMPTY;
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    /**
     * getUUID 生成uuid，采用 jdk 9 的形式，优化性能
     *
     * @since 2020/3/23 17:36
     * @return java.lang.String
     */
    public String getUUID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long lsb = random.nextLong();
        long msb = random.nextLong();
        byte[] buf = new byte[32];
        formatUnsignedLong(lsb, buf, 20, 12);
        formatUnsignedLong(lsb >>> 48, buf, 16, 4);
        formatUnsignedLong(msb, buf, 12, 4);
        formatUnsignedLong(msb >>> 16, buf, 8,  4);
        formatUnsignedLong(msb >>> 32, buf, 0,  8);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void formatUnsignedLong(long val, byte[] buf, int offset, int len) {
        int charPos = offset + len;
        int radix = 1 << 4;
        int mask = radix - 1;
        do {
            buf[--charPos] = CharsetUtils.DIGITS[((int) val) & mask];
            val >>>= 4;
        } while (charPos > offset);
    }

}
