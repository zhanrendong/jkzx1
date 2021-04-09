package tech.tongyu.bct.auth.exception.manager;

import tech.tongyu.bct.common.exception.CustomException;

import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.common.exception.ErrorCode;

public class DaoException extends CustomException {
    public DaoException() {
        super(ErrorCode.SERVICE_FAILED, ReturnMessageAndTemplateDef.Errors.DATABASE_ERROR.getMessage());
    }
}
