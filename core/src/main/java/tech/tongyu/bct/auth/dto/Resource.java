package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.util.tree.TreeEntity;

import java.util.Collection;

public class Resource extends TreeEntity<Resource> {

    @BctField(
            name = "resourceName",
            description = "资源名称",
            type = "String",
            order = 1
    )
    private String resourceName;
    @BctField(
            name = "resourceType",
            description = "资源类型",
            type = "ResourceTypeEnum",
            order = 2,
            componentClass = ResourceTypeEnum.class
    )
    private ResourceTypeEnum resourceType;
    @BctField(
            name = "departmentId",
            description = "部门ID",
            type = "String",
            order = 3
    )
    private String departmentId;
    @BctField(
            name = "resourcePermissions",
            description = "权限列表",
            type = "Collection<ResourcePermissionTypeEnum>",
            order = 4,
            isCollection = true,
            componentClass = ResourcePermissionTypeEnum.class
    )
    private Collection<ResourcePermissionTypeEnum> resourcePermissions;

    public Resource(String id, Integer sort, Resource parent
            , String resourceName, ResourceTypeEnum resourceType, String departmentId, Collection<ResourcePermissionTypeEnum> resourcePermissions) {
        super(id, sort, parent);
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.departmentId = departmentId;
        this.resourcePermissions = resourcePermissions;
    }

    public Resource(String id, Integer sort, String resourceName
            , ResourceTypeEnum resourceType, String departmentId, Collection<ResourcePermissionTypeEnum> resourcePermissions) {
        super(id, sort);
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.departmentId = departmentId;
        this.resourcePermissions = resourcePermissions;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public ResourceTypeEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceTypeEnum resourceType) {
        this.resourceType = resourceType;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public Collection<ResourcePermissionTypeEnum> getResourcePermissions() {
        return resourcePermissions;
    }

    public void setResourcePermissions(Collection<ResourcePermissionTypeEnum> resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }
}
