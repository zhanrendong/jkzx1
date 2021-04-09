package tech.tongyu.bct.auth.enums;

import org.apache.commons.lang3.StringUtils;

import tech.tongyu.bct.auth.enums.exception.AuthEnumParseException;
import tech.tongyu.bct.auth.enums.exception.ReturnMessageAndTemplateDef;

public enum UserTypeEnum {
    NORMAL,
    SCRIPT;

    public static UserTypeEnum of(String userType){
        try{
            return UserTypeEnum.valueOf(StringUtils.upperCase(userType));
        } catch (IllegalArgumentException e){
            throw new AuthEnumParseException(ReturnMessageAndTemplateDef.Errors.INVALID_USER_TYPE, userType);
        }
    }
}
