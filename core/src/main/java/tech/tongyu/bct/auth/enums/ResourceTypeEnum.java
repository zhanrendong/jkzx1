package tech.tongyu.bct.auth.enums;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.auth.enums.exception.AuthEnumParseException;
import tech.tongyu.bct.auth.enums.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.common.api.doc.BctField;

public enum ResourceTypeEnum {
    @BctField(description = "交易簿")
    BOOK("交易簿"),
    @BctField(description = "交易")
    TRADE("交易"),
    @BctField(description = "投资组合")
    PORTFOLIO("投资组合"),
    @BctField(description = "资源")
    NAMESPACE("资源"),
    @BctField(description = "根资源")
    ROOT("根资源"),
    @BctField(description = "部门")
    DEPT("部门"),
    @BctField(description = "公司")
    COMPANY("公司"),
    @BctField(description = "用户")
    USER("用户"),
    @BctField(description = "角色")
    ROLE("角色"),
    @BctField(description = "保证金")
    MARGIN("保证金"),
    @BctField(description = "客户信息")
    CLIENT_INFO("客户信息"),
    @BctField(description = "审批组管理")
    APPROVAL_GROUP("审批组管理"),
    @BctField(description = "审批组信息")
    APPROVAL_GROUP_INFO("审批组信息"),
    @BctField(description = "流程定义")
    PROCESS_DEFINITION("流程定义"),
    @BctField(description = "流程定义信息")
    PROCESS_DEFINITION_INFO("流程定义信息"),
    @BctField(description = "触发器管理")
    TRIGGER("触发器管理"),
    @BctField(description = "触发器信息")
    TRIGGER_INFO("触发器信息"),
    ;
    @BctField(ignore = true)
    private String alias;
    ResourceTypeEnum(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public static ResourceTypeEnum of(String resourceType){
        try{
            return ResourceTypeEnum.valueOf(StringUtils.upperCase(resourceType));
        } catch (IllegalArgumentException e){
            throw new AuthEnumParseException(ReturnMessageAndTemplateDef.Errors.INVALID_RESOURCE_TYPE, resourceType);
        }
    }
}
