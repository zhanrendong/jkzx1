package tech.tongyu.bct.risk.excepitons;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class RiskControlException extends CustomException {

    public RiskControlException(ReturnMessageAndTemplateDef.Errors error, Object... params) {
        super(ErrorCode.SERVICE_FAILED, error.getMessage(params));
    }
}
