package tech.tongyu.bct.service.quantlib.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Quant API argument annotation
 * Created by lu on 5/4/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BctQuantApiArg {
    String name();
    String description() default "";
    String type() default "Json";
    boolean required() default true;
}
