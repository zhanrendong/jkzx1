package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.Collection;

public class RolePageComponentDTO {

    @BctField(
            name = "roleId",
            description = "角色ID",
            type = "String",
            order = 1
    )
    private String roleId;
    @BctField(
            name = "pageComponentId",
            description = "页面ID",
            type = "Collection<String>",
            order = 2
    )
    private Collection<String> pageComponentId;

    public RolePageComponentDTO(String roleId, Collection<String> pageComponentId) {
        this.roleId = roleId;
        this.pageComponentId = pageComponentId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
