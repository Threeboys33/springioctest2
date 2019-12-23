package dtstack.san.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
