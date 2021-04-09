package tech.tongyu.bct.common.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BctMethodInfo {
    String description() default "";
    String retName() default "result";
    String retDescription() default "";
    BctExcelTypeEnum excelType() default BctExcelTypeEnum.JavaType;
    BctApiTagEnum[] tags() default {};
    Class<?> returnClass() default Object.class;
    String service() default "";
    boolean enableLogging() default false;
}
