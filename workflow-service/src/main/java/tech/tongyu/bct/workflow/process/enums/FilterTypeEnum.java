package tech.tongyu.bct.workflow.process.enums;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public enum FilterTypeEnum {

    /**
     * task readable
     */
    TASK_READABLE(0),
    /**
     * task completable
     */
    TASK_COMPLETABLE(1),

    /**
     * process startable
     */
    PROCESS_STARTABLE(2);

    private Integer code;

    FilterTypeEnum(int code){
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
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_FILTER_TYPE, itemType);
        }
    }

}
