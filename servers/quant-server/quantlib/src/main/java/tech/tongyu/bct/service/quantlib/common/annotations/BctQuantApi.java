package tech.tongyu.bct.service.quantlib.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BctQuantApi {
    String name();
    String description();

    String[] argNames();
    String[] argTypes();
    String[] argDescriptions();

    String retName();
    String retType();
    String retDescription();

    boolean addIdInput() default true;
}
