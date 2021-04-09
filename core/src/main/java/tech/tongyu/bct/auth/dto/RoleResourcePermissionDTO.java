package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

public class RoleResourcePermissionDTO {

    private String roleId;
    private String resourceId;
    private ResourcePermissionTypeEnum resourcePermissionType;

    public RoleResourcePermissionDTO(String roleId, String resourceId, ResourcePermissionTypeEnum resourcePermissionType) {
        this.roleId = roleId;
        this.resourceId = resourceId;
        this.resourcePermissionType = resourcePermissionType;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public ResourcePermissionTypeEnum getResourcePermissionType() {
        return resourcePermissionType;
    }

    public void setResourcePermissionType(ResourcePermissionTypeEnum resourcePermissionType) {
        this.resourcePermissionType = resourcePermissionType;
    }
}
