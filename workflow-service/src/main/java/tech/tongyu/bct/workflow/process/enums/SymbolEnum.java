package tech.tongyu.bct.workflow.process.enums;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public enum SymbolEnum {
    /**
     * >
     */
    GT(0),
    /**
     * <
     */
    LT(1),
    /**
     * =
     */
    EQ(2),
    /**
     * >=
     */
    GE(3),
    /**
     * =<
     */
    LE(4),
    /**
     * !=
     */
    NE(5),
    ;

    private Integer code;

    SymbolEnum(int code){
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
