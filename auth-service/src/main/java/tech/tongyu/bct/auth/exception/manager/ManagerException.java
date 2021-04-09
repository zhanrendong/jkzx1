package tech.tongyu.bct.auth.exception.manager;

import tech.tongyu.bct.common.exception.CustomException;

import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.common.exception.ErrorCode;

public class ManagerException extends CustomException {
    public ManagerException(ReturnMessageAndTemplateDef.Errors error, Object...params) {
        super(ErrorCode.SERVICE_FAILED, error.getMessage(params));
    }
}
