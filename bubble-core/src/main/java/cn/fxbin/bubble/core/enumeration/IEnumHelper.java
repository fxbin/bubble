package cn.fxbin.bubble.core.enumeration;

import cn.fxbin.bubble.core.exception.InvalidEnumValueException;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IEnumHelper
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/10/26 11:49
 */
@UtilityClass
public class IEnumHelper {

    /**
     * 枚举值是否存在
     *
     * @param value 枚举值
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return true 存在，false 不存在
     */
    public <T extends IEnum> boolean existByValue(int value, T[] enums) {
        return Arrays.stream(enums)
                .anyMatch(e -> e.value() == value);
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象
     */
    public <T extends IEnum> T valueOf(int value, T[] enums) {
        return Arrays.stream(enums)
                .filter(e -> e.value() == value)
                .findAny()
                .orElseThrow(InvalidEnumValueException::new);
    }

    /**
     * 获取枚举对象列表
     *
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象列表
     */
    public <T extends IEnum> List<T> allOf(T[] enums) {
        return Arrays.stream(enums)
                .filter(e -> e.value() != -1)
                .collect(Collectors.toList());
    }

}
