package tech.tongyu.bct.service.quantlib.common.utils;

/**
 * Created by jinkepeng on 2017/7/10.
 */
public class CustomException extends RuntimeException {
    public CustomException(){

    }

    public CustomException(String message){
        super(message);
    }
}
