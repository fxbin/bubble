package cn.fxbin.bubble.web.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import java.lang.reflect.Field;

/**
 * EqualFieldValidator 相等值校验器
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/15 12:40
 */
@Slf4j
public class EqualFieldValidator implements ConstraintValidator<EqualField, Object> {

    private String source;
    private String target;

    @Override
    public void initialize(EqualField constraintAnnotation) {
        this.source = constraintAnnotation.source();
        this.target = constraintAnnotation.target();
    }

    /**
     * isValid
     *
     * @author fxbin
     * @since 2020/11/15 12:48
     * @param object 传入值对象
     * @param context 上下文
     * @return boolean
     */
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        Class<?> clazz = object.getClass();

        Field srcField = ReflectionUtils.findField(clazz, this.source);
        Field dstField = ReflectionUtils.findField(clazz, this.target);

        try {

            if (srcField == null || dstField == null) {
                throw new ValidationException("反射获取变量失败");
            }

            srcField.setAccessible(true);
            dstField.setAccessible(true);
            Object src = srcField.get(object);
            Object dst = dstField.get(object);

            // 其中一个变量为 null 时，则必须两个都为 null 才相等
            if (src == null || dst == null) {
                return src == dst;
            }

            // 如果两个对象内存地址相同，则一定相等
            if (src.equals(dst)) {
                return true;
            }

            // 调用 equals 方法比较
            return src.equals(dst);
        } catch (Exception e) {
            log.warn("EqualFieldValidator 校验异常", e);
            return false;
        }
    }
}
