package dtstack.san.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoWrite {
    String value() default "";
}
