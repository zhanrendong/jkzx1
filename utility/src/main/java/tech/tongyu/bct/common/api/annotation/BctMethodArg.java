package tech.tongyu.bct.common.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum.JavaType;

@Retention(RetentionPolicy.RUNTIME)
public @interface BctMethodArg {
    String name() default "";
    String description() default "";
    String excelName() default "";
    BctExcelTypeEnum excelType() default JavaType;
    boolean required() default true;
    Class<?> argClass() default Object.class;
}
