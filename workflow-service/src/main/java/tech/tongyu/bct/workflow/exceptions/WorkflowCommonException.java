package tech.tongyu.bct.workflow.exceptions;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

public class WorkflowCommonException extends CustomException {

    public WorkflowCommonException(ReturnMessageAndTemplateDef.Errors error, Object... params) {
        super(ErrorCode.SERVICE_FAILED, error.getMessage(params));
    }

}
