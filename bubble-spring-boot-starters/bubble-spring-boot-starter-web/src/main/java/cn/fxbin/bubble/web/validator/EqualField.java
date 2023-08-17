package cn.fxbin.bubble.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * EqualField
 *
 * <p>
 *     比较两个属性是否相等
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/15 12:40
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {EqualFieldValidator.class})
public @interface EqualField {

    /**
     * 源属性
     */
    String source();

    /**
     * 目标属性
     */
    String target();

    String message() default "both fields must be equal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
