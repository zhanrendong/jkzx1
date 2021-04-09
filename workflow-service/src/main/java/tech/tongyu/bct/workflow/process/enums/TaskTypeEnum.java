package tech.tongyu.bct.workflow.process.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

/**
 * @author yongbin
 */
public enum TaskTypeEnum {

    /**
     * input data
     */
    INPUT_DATA("insertData"),
    /**
     * modify data
     */
    MODIFY_DATA("modifyData"),
    /**
     * review data
     */
    REVIEW_DATA("reviewData"),
    /**
     * none, used by process scope object
     */
    NONE("none"),
    /**
     * counter sign data
     */
    COUNTER_SIGN_DATA("counterSignData");

    private String alias;
    TaskTypeEnum(String alias) {
        this.alias = alias;
    }

    @JsonValue
    public String getAlias() {
        return alias;
    }

    public static TaskTypeEnum of(String resourceType){
        try{
            return TaskTypeEnum.valueOf(StringUtils.upperCase(resourceType));
        } catch (IllegalArgumentException e){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.NODE_TYPE_ERROR, resourceType);
        }
    }
}
