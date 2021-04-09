package tech.tongyu.bct.common.util;

import java.util.Objects;

public class DoubleUtils {

    static class DoubleUtilsException extends RuntimeException{
        DoubleUtilsException(String message){
            super(message);
        }
    }

    public static boolean isDoubleValid(Double val){
        return !Objects.isNull(val)
                && !Double.isNaN(val);
    }

    public static void requireValidDouble(Double val, String valName, String format){
        if (!DoubleUtils.isDoubleValid(val))
            throw new DoubleUtilsException(String.format(format, valName, val));
    }

    /**
     * WARNING: the number of vals & valNameList should be the same.
     * @param vals
     * @param valNameList
     * @param format
     */
    public static void requireValidDouble(Double[] vals, String[] valNameList, String format){
        if(CollectionUtils.isNotEmpty(vals)){
            int length = vals.length;
            for(int i = 0; i < length; i++){
                requireValidDouble(vals[i], valNameList[i], format);
            }
        }
    }

    public static Double getDoubleOrNan(Double val){
        if(!DoubleUtils.isDoubleValid(val))
            return Double.NaN;
        return val;
    }

    public static Boolean equalsZero(Double val){
        return Math.abs(val) < 10E-200;
    }

    public static Double num2Double(Object val) {
        if(Objects.isNull(val)) return null;
        if(Double.class.isInstance(val) || Float.class.isInstance(val)){
            return (Double)val;
        } else if(Integer.class.isInstance(val)
                ||Short.class.isInstance(val)
                ||Long.class.isInstance(val)) {
            return (double) ((Integer) val).longValue();
        } else if(String.class.isInstance(val)){
            try {
                return Double.valueOf((String) val);
            }
            catch (NumberFormatException e){
                return Double.NaN;
            }
        } else {
            return Double.NaN;
        }
    }

    public static Double num2Double(Object val, boolean checkValid){
        Double ret = num2Double(val);
        if(checkValid){
            if(!isDoubleValid(ret))
                throw new DoubleUtilsException(String.format("not a valid double: %s", ret));
        }
        return ret;
    }

    public static Integer num2Integer(Object val) {
        if(Objects.isNull(val)) return null;
        if(Double.class.isInstance(val) || Float.class.isInstance(val)){
            return (int)(((Double)val) + 0.5);
        } else if(Integer.class.isInstance(val)
                ||Short.class.isInstance(val)
                ||Long.class.isInstance(val)) {
            return (int) ((Integer) val).longValue();
        } else if(String.class.isInstance(val)){
            try {
                return Integer.valueOf((String) val);
            }
            catch (NumberFormatException e){
                return null;
            }
        } else {
            return null;
        }
    }
}
