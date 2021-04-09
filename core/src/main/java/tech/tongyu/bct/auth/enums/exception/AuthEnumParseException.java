package tech.tongyu.bct.auth.enums.exception;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

public class AuthEnumParseException extends CustomException {
    public AuthEnumParseException(ReturnMessageAndTemplateDef.Errors error, Object... templateParams) {
        super(ErrorCode.SERVICE_FAILED, error.getMessage(templateParams));
    }
}
