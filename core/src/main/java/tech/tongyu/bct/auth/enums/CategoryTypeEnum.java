package tech.tongyu.bct.auth.enums;

import org.apache.commons.lang3.StringUtils;

import tech.tongyu.bct.auth.enums.exception.AuthEnumParseException;
import tech.tongyu.bct.auth.enums.exception.ReturnMessageAndTemplateDef;

public enum CategoryTypeEnum {
    UNIVERSAL(0);//通用

    private Integer code;

    CategoryTypeEnum(int code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static CategoryTypeEnum of(String categoryType){
        try{
            return CategoryTypeEnum.valueOf(StringUtils.upperCase(categoryType));
        } catch (IllegalArgumentException e){
            throw new AuthEnumParseException(ReturnMessageAndTemplateDef.Errors.INVALID_CATEGORY_TYPE, categoryType);
        }
    }
}
