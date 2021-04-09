package tech.tongyu.bct.auth.enums;

import org.apache.commons.lang3.StringUtils;

import tech.tongyu.bct.auth.enums.exception.AuthEnumParseException;
import tech.tongyu.bct.auth.enums.exception.ReturnMessageAndTemplateDef;

public enum SettingTypeEnum {
    USER(0),
    SYSTEM(1);

    private Integer code;

    SettingTypeEnum(int code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static SettingTypeEnum of(String settingType){
        try{
            return SettingTypeEnum.valueOf(StringUtils.upperCase(settingType));
        } catch (IllegalArgumentException e){
            throw new AuthEnumParseException(ReturnMessageAndTemplateDef.Errors.INVALID_SETTING_TYPE, settingType);
        }
    }
}
