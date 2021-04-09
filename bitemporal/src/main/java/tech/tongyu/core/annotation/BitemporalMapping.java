package tech.tongyu.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BitemporalMapping {
    String column() default "";

    String name() default "";

    boolean isList() default false;

    boolean isEntity() default true;
}
