package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public class RoleDTO {

    @BctField(
            name = "id",
            description = "角色ID",
            type = "string"
    )
    private String id;
    @BctField(
            name = "roleName",
            description = "角色名称",
            type = "string",
            order = 2
    )
    private String roleName;
    @BctField(
            name = "remark",
            description = "备注",
            type = "string",
            order = 3
    )
    private String remark;
    @BctField(
            name = "alias",
            description = "别名",
            type = "string",
            order = 4
    )
    private String alias;

    public RoleDTO(String id, String roleName, String remark, String alias) {
        this.id = id;
        this.roleName = roleName;
        this.remark = remark;
        this.alias = alias;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
