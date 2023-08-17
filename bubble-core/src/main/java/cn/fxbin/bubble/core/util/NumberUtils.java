package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.constant.NumPool;
import cn.hutool.core.util.NumberUtil;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * NumberUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/23 17:38
 */
@UtilityClass
public class NumberUtils extends NumberUtil {

    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtil.toInt(null) = 0
     *   NumberUtil.toInt("")   = 0
     *   NumberUtil.toInt("1")  = 1
     * </pre>
     *
     * @param str the string to convert, may be null
     * @return the int represented by the string, or <code>zero</code> if
     * conversion fails
     */
    public int toInt(final String str) {
        return toInt(str, 0);
    }

    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtil.toInt(null, 1) = 1
     *   NumberUtil.toInt("", 1)   = 1
     *   NumberUtil.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str          the string to convert, may be null
     * @param defaultValue the default value
     * @return the int represented by the string, or the default if conversion fails
     */
    public int toInt(@Nullable final String str, final int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtil.toLong(null) = 0L
     *   NumberUtil.toLong("")   = 0L
     *   NumberUtil.toLong("1")  = 1L
     * </pre>
     *
     * @param str the string to convert, may be null
     * @return the long represented by the string, or <code>0</code> if
     * conversion fails
     */
    public long toLong(final String str) {
        return toLong(str, 0L);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtil.toLong(null, 1L) = 1L
     *   NumberUtil.toLong("", 1L)   = 1L
     *   NumberUtil.toLong("1", 0L)  = 1L
     * </pre>
     *
     * @param str          the string to convert, may be null
     * @param defaultValue the default value
     * @return the long represented by the string, or the default if conversion fails
     */
    public long toLong(@Nullable final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }


    /**
     * <p>Convert a <code>String</code> to a <code>float</code>, returning
     * <code>0.0f</code> if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>,
     * <code>0.0f</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toFloat(null)   = 0.0f
     *   NumberUtils.toFloat("")     = 0.0f
     *   NumberUtils.toFloat("1.5")  = 1.5f
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @return the float represented by the string, or <code>0.0f</code>
     *  if conversion fails
     * @since 2.1
     */
    public float toFloat(final String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>float</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>, the default
     * value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toFloat(null, 1.1f)   = 1.0f
     *   NumberUtils.toFloat("", 1.1f)     = 1.1f
     *   NumberUtils.toFloat("1.5", 0.0f)  = 1.5f
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @param defaultValue the default value
     * @return the float represented by the string, or defaultValue
     *  if conversion fails
     * @since 2.1
     */
    public float toFloat(final String str, final float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>double</code>, returning
     * <code>0.0d</code> if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>,
     * <code>0.0d</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null)   = 0.0d
     *   NumberUtils.toDouble("")     = 0.0d
     *   NumberUtils.toDouble("1.5")  = 1.5d
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @return the double represented by the string, or <code>0.0d</code>
     *  if conversion fails
     * @since 2.1
     */
    public double toDouble(final String str) {
        return toDouble(str, 0.0d);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>double</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>, the default
     * value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null, 1.1d)   = 1.1d
     *   NumberUtils.toDouble("", 1.1d)     = 1.1d
     *   NumberUtils.toDouble("1.5", 0.0d)  = 1.5d
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @param defaultValue the default value
     * @return the double represented by the string, or defaultValue
     *  if conversion fails
     * @since 2.1
     */
    public double toDouble(final String str, final double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>BigDecimal</code> to a <code>double</code>.</p>
     *
     * <p>If the <code>BigDecimal</code> <code>value</code> is
     * <code>null</code>, then the specified default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null)                     = 0.0d
     *   NumberUtils.toDouble(BigDecimal.valudOf(8.5d)) = 8.5d
     * </pre>
     *
     * @param value the <code>BigDecimal</code> to convert, may be <code>null</code>.
     * @return the double represented by the <code>BigDecimal</code> or
     *  <code>0.0d</code> if the <code>BigDecimal</code> is <code>null</code>.
     * @since 3.8
     */
    public double toDouble(final BigDecimal value) {
        return toDouble(value, 0.0d);
    }

    /**
     * <p>Convert a <code>BigDecimal</code> to a <code>double</code>.</p>
     *
     * <p>If the <code>BigDecimal</code> <code>value</code> is
     * <code>null</code>, then the specified default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null, 1.1d)                     = 1.1d
     *   NumberUtils.toDouble(BigDecimal.valudOf(8.5d), 1.1d) = 8.5d
     * </pre>
     *
     * @param value the <code>BigDecimal</code> to convert, may be <code>null</code>.
     * @param defaultValue the default value
     * @return the double represented by the <code>BigDecimal</code> or the
     *  defaultValue if the <code>BigDecimal</code> is <code>null</code>.
     * @since 3.8
     */
    public double toDouble(final BigDecimal value, final double defaultValue) {
        return value == null ? defaultValue : value.doubleValue();
    }


    /**
     * <p>Convert a <code>String</code> to a <code>byte</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toByte(null) = 0
     *   NumberUtils.toByte("")   = 0
     *   NumberUtils.toByte("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the byte represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.5
     */
    public byte toByte(final String str) {
        return toByte(str, (byte) 0);
    }


    /**
     * <p>Convert a <code>String</code> to a <code>byte</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toByte(null, 1) = 1
     *   NumberUtils.toByte("", 1)   = 1
     *   NumberUtils.toByte("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the byte represented by the string, or the default if conversion fails
     * @since 2.5
     */
    public byte toByte(final String str, final byte defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }


    /**
     * <p>Convert a <code>String</code> to a <code>short</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toShort(null) = 0
     *   NumberUtils.toShort("")   = 0
     *   NumberUtils.toShort("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the short represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.5
     */
    public short toShort(final String str) {
        return toShort(str, (short) 0);
    }

    /**
     * <p>Convert a <code>String</code> to an <code>short</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toShort(null, 1) = 1
     *   NumberUtils.toShort("", 1)   = 1
     *   NumberUtils.toShort("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the short represented by the string, or the default if conversion fails
     * @since 2.5
     */
    public short toShort(final String str, final short defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Convert a <code>BigDecimal</code> to a <code>BigDecimal</code> with a scale of
     * two that has been rounded using <code>RoundingMode.HALF_EVEN</code>. If the supplied
     * <code>value</code> is null, then <code>BigDecimal.ZERO</code> is returned.
     *
     * <p>Note, the scale of a <code>BigDecimal</code> is the number of digits to the right of the
     * decimal point.</p>
     *
     * @param value the <code>BigDecimal</code> to convert, may be null.
     * @return the scaled, with appropriate rounding, <code>BigDecimal</code>.
     * @since 3.8
     */
    public BigDecimal toScaledBigDecimal(final BigDecimal value) {
        return toScaledBigDecimal(value, NumPool.NUM_OF_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert a <code>BigDecimal</code> to a <code>BigDecimal</code> whose scale is the
     * specified value with a <code>RoundingMode</code> applied. If the input <code>value</code>
     * is <code>null</code>, we simply return <code>BigDecimal.ZERO</code>.
     *
     * @param value the <code>BigDecimal</code> to convert, may be null.
     * @param scale the number of digits to the right of the decimal point.
     * @param roundingMode a rounding behavior for numerical operations capable of
     *  discarding precision.
     * @return the scaled, with appropriate rounding, <code>BigDecimal</code>.
     * @since 3.8
     */
    public BigDecimal toScaledBigDecimal(final BigDecimal value, final int scale, final RoundingMode roundingMode) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(
                scale,
                (roundingMode == null) ? RoundingMode.HALF_EVEN : roundingMode
        );
    }

    /**
     * Convert a <code>Float</code> to a <code>BigDecimal</code> with a scale of
     * two that has been rounded using <code>RoundingMode.HALF_EVEN</code>. If the supplied
     * <code>value</code> is null, then <code>BigDecimal.ZERO</code> is returned.
     *
     * <p>Note, the scale of a <code>BigDecimal</code> is the number of digits to the right of the
     * decimal point.</p>
     *
     * @param value the <code>Float</code> to convert, may be null.
     * @return the scaled, with appropriate rounding, <code>BigDecimal</code>.
     * @since 3.8
     */
    public BigDecimal toScaledBigDecimal(final Float value) {
        return toScaledBigDecimal(value, NumPool.NUM_OF_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert a <code>Float</code> to a <code>BigDecimal</code> whose scale is the
     * specified value with a <code>RoundingMode</code> applied. If the input <code>value</code>
     * is <code>null</code>, we simply return <code>BigDecimal.ZERO</code>.
     *
     * @param value the <code>Float</code> to convert, may be null.
     * @param scale the number of digits to the right of the decimal point.
     * @param roundingMode a rounding behavior for numerical operations capable of
     *  discarding precision.
     * @return the scaled, with appropriate rounding, <code>BigDecimal</code>.
     * @since 3.8
     */
    public BigDecimal toScaledBigDecimal(final Float value, final int scale, final RoundingMode roundingMode) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(
                BigDecimal.valueOf(value),
                scale,
                roundingMode
        );
    }
    
}
