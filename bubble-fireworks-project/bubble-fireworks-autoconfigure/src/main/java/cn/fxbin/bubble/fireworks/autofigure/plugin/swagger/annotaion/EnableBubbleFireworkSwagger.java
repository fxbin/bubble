package cn.fxbin.bubble.fireworks.autofigure.plugin.swagger.annotaion;

import cn.fxbin.bubble.fireworks.autofigure.plugin.swagger.SwaggerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableBubbleFireworkSwagger
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/31 18:49
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SwaggerAutoConfiguration.class})
public @interface EnableBubbleFireworkSwagger {
}
