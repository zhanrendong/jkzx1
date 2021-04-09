package tech.tongyu.bct.exchange.common;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.common.exception.CustomException;

import java.lang.reflect.Method;
import java.util.Objects;

public class FieldUtils {

    public static Method getSetMethod(Object src, String fieldName) {
        Objects.requireNonNull(src);
        if(StringUtils.isBlank(fieldName))
            throw new CustomException("fieldName cannot be blank");
        try {
            return src.getClass().getDeclaredMethod("set" + StringUtils.capitalize(fieldName), getFieldType(src, fieldName));
        } catch (SecurityException | NoSuchMethodException e){
            throw new CustomException("field with target name cannot be found");
        }
    }
    private static Class getFieldType(Object src, String fieldName){
        Objects.requireNonNull(src);
        if(StringUtils.isBlank(fieldName))
            throw new CustomException("fieldName cannot be blank");
        try {
            return src.getClass().getDeclaredField(fieldName).getType();
        } catch (NoSuchFieldException | SecurityException e){
            throw new CustomException("field with target name cannot be found");
        }

    }
}
