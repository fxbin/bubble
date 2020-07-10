package cn.fxbin.bubble.fireworks.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * MathUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/10 11:16
 */
@Slf4j
@UtilityClass
public class MathUtils {



    /**
     * multiply
     *
     * @since 2020/7/10 11:24
     * @param num1 数字
     * @param num2 数字
     * @return java.math.BigDecimal
     */
    public BigDecimal multiply(BigDecimal num1, BigDecimal num2){
        if(compareTo(num1, BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }else{
            return num1.multiply(num2);
        }
    }

    /**
     * divide
     *
     * @since 2020/7/10 11:23
     * @param num1 数字
     * @param num2 数字
     * @return java.math.BigDecimal
     */
    public BigDecimal divide(Integer num1, Integer num2) {
        Assert.notNull(num1, "num1 is not null");
        Assert.notNull(num2, "num2 is not null");
        return divide(BigDecimal.valueOf(num1), BigDecimal.valueOf(num2), 2);
    }

    /**
     * divide
     *
     * @since 2020/7/10 11:25
     * @param num1 数字
     * @param num2 数字
     * @param scale 小数位数
     * @return java.math.BigDecimal
     */
    public BigDecimal divide(BigDecimal num1, BigDecimal num2, Integer scale) {
        if(compareTo(num1, BigDecimal.ZERO) == 0 || compareTo(num2, BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }else{
            return num1.divide(num2, Optional.ofNullable(scale).orElse(2), BigDecimal.ROUND_UP);
        }
    }

    /**
     * compareTo
     *
     * 大于 时，返回 1;
     * 等于 时，返回 0;
     * 小于 时，返回 -1;
     *
     * @since 2020/7/10 11:24
     * @param num1 数字
     * @param num2 数字
     * @return java.lang.Integer
     */
    public Integer compareTo(BigDecimal num1, BigDecimal num2){
        return num1.compareTo(num2);
    }

}
