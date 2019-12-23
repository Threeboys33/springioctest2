package dtstack.san.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value() default "";
}
