package tech.tongyu.bct.common.util;

import java.util.Objects;

public class IntegerUtils {
    public static Integer num2Integer(Object val) {
        if(Objects.isNull(val)) throw new IllegalArgumentException("argument for num2Integer is null");
        if(val instanceof Integer){
            return (Integer)val;
        }else if(val instanceof String){
            return Integer.valueOf((String)val);
        }
        throw new UnsupportedOperationException(String.format("target obj[%s] can't be converted to integer", val));
    }

    public static Integer num2IntegerOrNull(Object val){
        if(val instanceof Integer){
            return (Integer)val;
        }else if(val instanceof String){
            return Integer.valueOf((String)val);
        }
        else{
            return null;
        }
    }
}
