package tech.tongyu.bct.auth.exception.service;

import tech.tongyu.bct.common.exception.CustomException;

import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.common.exception.ErrorCode;

public class ServiceException extends CustomException {
    public ServiceException(ReturnMessageAndTemplateDef.Errors error, Object...params) {
        super(ErrorCode.SERVICE_FAILED, error.getMessage(params));
    }
}
