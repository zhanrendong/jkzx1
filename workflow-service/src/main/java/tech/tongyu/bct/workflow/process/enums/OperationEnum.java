package tech.tongyu.bct.workflow.process.enums;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public enum OperationEnum {
    /**
     * as &
     */
    AND(0),
    /**
     * as |
     */
    OR(1),
    ;
    private Integer code;

    OperationEnum(int code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static FilterTypeEnum of(String itemType){
        try{
            return FilterTypeEnum.valueOf(StringUtils.upperCase(itemType));
        } catch (IllegalArgumentException e){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_OPERATION_TYPE, itemType);
        }
    }
}
