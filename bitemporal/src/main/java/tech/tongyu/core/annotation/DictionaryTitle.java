package tech.tongyu.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictionaryTitle {

    String tableName() default "";

    String tableMemo() default "";

    String parentTableName() default "";
}
