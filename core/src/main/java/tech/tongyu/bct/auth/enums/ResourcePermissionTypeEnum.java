package tech.tongyu.bct.auth.enums;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import tech.tongyu.bct.auth.enums.exception.AuthEnumParseException;
import tech.tongyu.bct.auth.enums.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.common.api.doc.BctField;

import java.util.List;
import java.util.stream.Collectors;

public enum ResourcePermissionTypeEnum {

    /*------------------------ role related  --------------------------*/
    @BctField(description = "创建角色")
    CREATE_ROLE("创建角色"),
    @BctField(description = "修改角色")
    UPDATE_ROLE("修改角色"),
    @BctField(description = "删除角色")
    DELETE_ROLE("删除角色"),
    /*------------------------ role related  --------------------------*/
    /*------------------------ common  --------------------------*/
    @BctField(description = "赋权")
    GRANT_ACTION("赋权"),
    @BctField(description = "查看资源")
    READ_RESOURCE("查看资源"),
    /*------------------------ common  --------------------------*/
    /*------------------------ user related  --------------------------*/
    @BctField(description = "创建用户")
    CREATE_USER("创建用户"),
    @BctField(description = "修改用户")
    UPDATE_USER("修改用户"),
    @BctField(description = "删除用户")
    DELETE_USER("删除用户"),
    @BctField(description = "创建脚本用户")
    CREATE_SCRIPT_USER("创建脚本用户"),
    @BctField(description = "查看用户")
    READ_USER("查看用户"),
    @BctField(description = "锁定用户")
    LOCK_USER("锁定用户"),
    @BctField(description = "解锁用户")
    UNLOCK_USER("解锁用户"),
    @BctField(description = "使用户到期")
    EXPIRE_USER("使用户到期"),
    @BctField(description = "解锁用户到期操作")
    UNEXPIRE_USER("解锁用户到期操作"),
    @BctField(description = "修改密码")
    CHANGE_PASSWORD("修改密码"),
    /*------------------------ user related  --------------------------*/
    /*------------------------ namespace related  --------------------------*/
    @BctField(description = "创建资源组")
    CREATE_NAMESPACE("创建资源组"),
    @BctField(description = "删除资源组")
    DELETE_NAMESPACE("删除资源组"),
    @BctField(description = "修改资源组")
    UPDATE_NAMESPACE("修改资源组"),
    @BctField(description = "修改投资组合")
    CREATE_PORTFOLIO("修改投资组合"),
    @BctField(description = "创建交易簿")
    CREATE_BOOK("创建交易簿"),
    /*------------------------ namespace related  --------------------------*/
    /*------------------------ trade related  --------------------------*/
    @BctField(description = "创建交易")
    CREATE_TRADE("创建交易"),
    @BctField(description = "更新")
    UPDATE_TRADE("更新"),
    @BctField(description = "删除交易")
    DELETE_TRADE("删除交易"),
    @BctField(description = "查看交易")
    READ_TRADE("查看交易"),
    /*------------------------ trade related  --------------------------*/
    /*------------------------ book related  --------------------------*/
    @BctField(description = "更新交易簿")
    UPDATE_BOOK("更新交易簿"),
    @BctField(description = "删除交易簿")
    DELETE_BOOK("删除交易簿"),
    @BctField(description = "查看交易簿")
    READ_BOOK("查看交易簿"),
    /*------------------------ book related  --------------------------*/
    /*------------------------ portfolio related  --------------------------*/
    @BctField(description = "查看投资组合")
    READ_PORTFOLIO("查看投资组合"),
    @BctField(description = "修改投资组合")
    UPDATE_PORTFOLIO("修改投资组合"),
    @BctField(description = "删除投资组合")
    DELETE_PORTFOLIO("删除投资组合"),
    /*------------------------ portfolio related  --------------------------*/
    /*------------------------ department related  --------------------------*/
    @BctField(description = "创建部门")
    CREATE_DEPARTMENT("创建部门"),
    @BctField(description = "更新部门")
    UPDATE_DEPARTMENT("更新部门"),
    @BctField(description = "删除部门")
    DELETE_DEPARTMENT("删除部门"),
    /*------------------------ department related  --------------------------*/
    /*------------------------ instrument related  --------------------------*/
    @BctField(description = "创建标的物")
    CREATE_INSTRUMENT("创建标的物"),
    @BctField(description = "删除标的物")
    DELETE_INSTRUMENT("删除标的物"),
    @BctField(description = "修改标的物")
    UPDATE_INSTRUMENT("修改标的物"),
    /*------------------------ instrument related  --------------------------*/

    /*------------------------ margin related ----------------------------- */
    @BctField(description = "修改保证金")
    UPDATE_MARGIN("修改保证金"),
    /*------------------------ margin related ----------------------------- */
    /*------------------------ client info related ----------------------------- */
    @BctField(description = "读取客户信息")
    READ_CLIENT("读取客户信息"),
    @BctField(description = "创建客户信息")
    CREATE_CLIENT("创建客户信息"),
    @BctField(description = "修改客户信息")
    UPDATE_CLIENT("修改客户信息"),
    @BctField(description = "删除客户信息")
    DELETE_CLIENT("删除客户信息"),
    /*------------------------ client info related ----------------------------- */
    /*------------------------ approval group related ----------------------------- */
    @BctField(description = "创建审批组")
    CREATE_APPROVAL_GROUP("创建审批组"),
    @BctField(description = "更新审批组")
    UPDATE_APPROVAL_GROUP("更新审批组"),
    @BctField(description = "删除审批组")
    DELETE_APPROVAL_GROUP("删除审批组"),
    @BctField(description = "更新审批组用户列表")
    UPDATE_APPROVAL_GROUP_USER("更新审批组用户列表"),
    @BctField(description = "审批组关联任务节点")
    UPDATE_TASK_NODE("审批组关联任务节点"),
    /*------------------------ approval group related ----------------------------- */
    /*------------------------ process node related ----------------------------- */
    @BctField(description = "流程定义与触发器管理创建")
    CREATE_PROCESS_AND_TRIGGER("流程定义与触发器管理创建"),
    @BctField(description = "流程定义修改")
    UPDATE_PROCESS_DEFINITION("流程定义修改"),
    /*------------------------ process node related ----------------------------- */
    /*------------------------ process trigger related ----------------------------- */
    @BctField(description = "流程绑定触发器")
    BIND_PROCESS_TRIGGER("流程绑定触发器"),
    @BctField(description = "创建触发器")
    CREATE_TRIGGER("创建触发器"),
    @BctField(description = "更新触发器")
    UPDATE_TRIGGER("更新触发器"),
    @BctField(description = "删除触发器")
    DELETE_TRIGGER("删除触发器"),
    /*------------------------ process trigger related ----------------------------- */
    ;

    public interface Arrays{

        List<ResourcePermissionTypeEnum> WHEN_CREATE_DEPARTMENT = Lists.newArrayList(
                GRANT_ACTION
                , CREATE_DEPARTMENT
                , UPDATE_DEPARTMENT
                , DELETE_DEPARTMENT
                , CREATE_NAMESPACE, READ_RESOURCE, DELETE_NAMESPACE, UPDATE_NAMESPACE
                , CREATE_BOOK
                , CREATE_PORTFOLIO
                , READ_USER
        );

        List<ResourcePermissionTypeEnum> WHEN_CREATE_NAMESPACE = Lists.newArrayList(
                GRANT_ACTION
                , CREATE_NAMESPACE, READ_RESOURCE, DELETE_NAMESPACE, UPDATE_NAMESPACE
                , CREATE_BOOK
                , CREATE_PORTFOLIO
        );

        List<ResourcePermissionTypeEnum> WHEN_CREATE_BOOK = Lists.newArrayList(
                GRANT_ACTION
                , UPDATE_BOOK, DELETE_BOOK, READ_BOOK
                , CREATE_TRADE, DELETE_TRADE, UPDATE_TRADE, READ_TRADE
        );

        List<ResourcePermissionTypeEnum> WHEN_CREATE_PORTFOLIO = Lists.newArrayList(
                GRANT_ACTION
                , UPDATE_PORTFOLIO, DELETE_PORTFOLIO, READ_PORTFOLIO
        );

        List<ResourcePermissionTypeEnum> ADMIN_ON_COMPANY = Lists.newArrayList(
                GRANT_ACTION, READ_USER
        );

        List<ResourcePermissionTypeEnum> WHEN_CREATE_APPROVAL_GROUP = Lists.newArrayList(
                GRANT_ACTION
                , UPDATE_APPROVAL_GROUP, DELETE_APPROVAL_GROUP, UPDATE_APPROVAL_GROUP_USER
        );

        List<ResourcePermissionTypeEnum> WHEN_CREATE_PROCESS_DEFINITION = Lists.newArrayList(
                GRANT_ACTION
                , UPDATE_PROCESS_DEFINITION
                , BIND_PROCESS_TRIGGER
        );
        List<ResourcePermissionTypeEnum> WHEN_CREATE_TRIGGER_INFO = Lists.newArrayList(
                GRANT_ACTION
                , UPDATE_TRIGGER
                , DELETE_TRIGGER
        );
        List<ResourcePermissionTypeEnum> WHEN_CREATE_TRIGGER = Lists.newArrayList(
                GRANT_ACTION
                , CREATE_TRIGGER
        );
    }

    @BctField(ignore = true)
    String alias;

    ResourcePermissionTypeEnum(String alias) {
        this.alias = alias;
    }

    public static ResourcePermissionTypeEnum getCreateResourcePermission(ResourceTypeEnum resourceTypeEnum){
        switch (resourceTypeEnum){
            case APPROVAL_GROUP_INFO:
                return CREATE_APPROVAL_GROUP;
            case TRIGGER_INFO:
                return CREATE_TRIGGER;
            case BOOK:
                return CREATE_BOOK;
            case PORTFOLIO:
                return CREATE_PORTFOLIO;
            case NAMESPACE:
                return CREATE_NAMESPACE;
            case DEPT:
                return CREATE_DEPARTMENT;
            case TRIGGER:
                return CREATE_PROCESS_AND_TRIGGER;
            default:
                return CREATE_NAMESPACE;
        }
    }

    public static List<ResourcePermissionTypeEnum> getResourcePermissionTypeEnumListWhenCreation(ResourceTypeEnum resourceTypeEnum){
        switch (resourceTypeEnum){
            case BOOK:
                return Arrays.WHEN_CREATE_BOOK;
            case NAMESPACE:
                return Arrays.WHEN_CREATE_NAMESPACE;
            case APPROVAL_GROUP_INFO:
                return Arrays.WHEN_CREATE_APPROVAL_GROUP;
            case PROCESS_DEFINITION_INFO:
                return Arrays.WHEN_CREATE_PROCESS_DEFINITION;
            case DEPT:
                return Arrays.WHEN_CREATE_DEPARTMENT;
            case PORTFOLIO:
                return Arrays.WHEN_CREATE_PORTFOLIO;
            case TRIGGER_INFO:
                return Arrays.WHEN_CREATE_TRIGGER_INFO;
            case TRIGGER:
                return Arrays.WHEN_CREATE_TRIGGER;
            default:
                return Arrays.WHEN_CREATE_NAMESPACE;
        }
    }

    public String getAlias() {
        return alias;
    }

    public static ResourcePermissionTypeEnum of(String resourcePermissionType){
        try{
            return ResourcePermissionTypeEnum.valueOf(StringUtils.upperCase(resourcePermissionType));
        } catch (IllegalArgumentException e){
            throw new AuthEnumParseException(ReturnMessageAndTemplateDef.Errors.INVALID_RESOURCE_PERMISSION_TYPE, resourcePermissionType);
        }
    }

    public static List<ResourcePermissionTypeEnum> ofList(List<String> permissions){
        return permissions.stream()
                .map(ResourcePermissionTypeEnum::of)
                .collect(Collectors.toList());
    }
}
