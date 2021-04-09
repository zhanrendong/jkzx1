package tech.tongyu.bct.auth.exception;

import tech.tongyu.bct.common.exception.CustomException;

import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.common.exception.ErrorCode;

public class AuthServiceException extends CustomException {

    public AuthServiceException(ReturnMessageAndTemplateDef.Errors error, Object... templateParams) {
        super(ErrorCode.SERVICE_FAILED, error.getMessage(templateParams));
    }
}
