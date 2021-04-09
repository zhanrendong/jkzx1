package tech.tongyu.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * API argument annotation
 * ExcelType: type hint to be used with excel add-in
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {

    String translator() default "";

    String description() default "";

    boolean required() default true;
}
