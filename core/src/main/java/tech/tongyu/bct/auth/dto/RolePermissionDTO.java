package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.util.Collection;

public class RolePermissionDTO {

    @BctField(
            name = "resourceId",
            description = "资源ID",
            type = "String",
            order = 1
    )
    private String resourceId;
    @BctField(
            name = "resourcePermissions",
            description = "权限列表",
            type = "Collection<ResourcePermissionTypeEnum>",
            order = 2,
            isCollection = true,
            componentClass = ResourcePermissionTypeEnum.class
    )
    private Collection<ResourcePermissionTypeEnum> resourcePermissions;

    public RolePermissionDTO(String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissions) {
        this.resourceId = resourceId;
        this.resourcePermissions = resourcePermissions;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
