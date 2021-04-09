package tech.tongyu.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * BCT API annotation
 *
 * This annotation indicates the function will be exposed as a RPC API
 *
 * Note: excelType is used only when the API is also exposed through excel add-in
 * Note: tags can be used to decide when and how the API should be exposed
 * Example use:
 *   excel: exposed to excel. otherwise hidden.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryTemplate {
    String description() default "";
//    String retName() default "";
//    String retDescription() default "";
}
