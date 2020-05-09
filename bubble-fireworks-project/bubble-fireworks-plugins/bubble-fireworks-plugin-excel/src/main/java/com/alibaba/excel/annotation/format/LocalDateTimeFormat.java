package com.alibaba.excel.annotation.format;


import java.lang.annotation.*;

/**
 * LocalDateTimeFormat
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 12:51
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LocalDateTimeFormat {

    /**
     * Specific format reference {@link java.time.format.DateTimeFormatter}
     *
     * @since 2020/4/7 12:52
     * @return java.lang.String
     */
    String value() default "";

}
