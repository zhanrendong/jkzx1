package tech.tongyu.bct.workflow.process.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

public enum ProcessInstanceStatusEnum {

    PROCESS_FINISH("processFinish"),
    PROCESS_UNFINISHED("processUnfinished"),
    PROCESS_ABANDON("processAbandon")
    ;

    private String code;

    ProcessInstanceStatusEnum(String code){
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static ProcessInstanceStatusEnum of(String itemType){
        try{
            return ProcessInstanceStatusEnum.valueOf(StringUtils.upperCase(itemType));
        } catch (IllegalArgumentException e){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_PROCESS_INSTANCE_STATUS, itemType);
        }
    }
}
