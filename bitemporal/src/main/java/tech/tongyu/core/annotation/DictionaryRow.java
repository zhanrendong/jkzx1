package tech.tongyu.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictionaryRow {

    String columnName() default "";

    String columnMemo() default "";

    String columnType() default "";

    String remark() default "";
}
