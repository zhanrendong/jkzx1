package tech.tongyu.bct.common.api.doc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BctField {
    String name() default "";
    String description() default "";
    String type() default "";
    boolean isCollection() default false;
    Class<?> componentClass() default Object.class;
    boolean ignore() default false;
    int order() default 1;
    Class<?>[] possibleComponentClassCollection() default {};
}
