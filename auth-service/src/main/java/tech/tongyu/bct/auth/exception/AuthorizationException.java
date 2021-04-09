package tech.tongyu.bct.auth.exception;

import tech.tongyu.bct.common.exception.CustomException;

import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.common.exception.ErrorCode;

public class AuthorizationException extends CustomException {

    public AuthorizationException(ResourceTypeEnum resourceType, String resourceName, ResourcePermissionTypeEnum operation) {
        super(ErrorCode.SERVICE_FAILED, ReturnMessageAndTemplateDef.Errors.UNAUTHORIZATION_ACTION.getMessage(resourceType.getAlias(), resourceName , operation.getAlias()));

    }
}