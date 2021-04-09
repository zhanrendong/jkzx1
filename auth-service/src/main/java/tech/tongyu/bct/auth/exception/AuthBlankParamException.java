package tech.tongyu.bct.auth.exception;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

import java.util.Arrays;

public class AuthBlankParamException extends CustomException {

    public AuthBlankParamException(String... templateParams) {
        super(ErrorCode.INPUT_NOT_VALID, ReturnMessageAndTemplateDef.Errors.EMPTY_PARAM.getMessage(Arrays.toString(templateParams)));
    }

}
