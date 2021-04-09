package tech.tongyu.bct.workflow.process.enums;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

public enum ProcessInstanceUserPerspectiveEnum {
    STARTED_BY_ME(0),
    EXECUTED_BY_ME(1);

    private Integer code;

    ProcessInstanceUserPerspectiveEnum(int code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static ProcessInstanceUserPerspectiveEnum of(String itemType){
        try{
            return ProcessInstanceUserPerspectiveEnum.valueOf(StringUtils.upperCase(itemType));
        } catch (IllegalArgumentException e){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_PROCESS_INSTANCE_STATUS, itemType);
        }
    }

}
