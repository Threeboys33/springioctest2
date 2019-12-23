package dtstack.san.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
}
