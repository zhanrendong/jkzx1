package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

public class DepartmentWithResourceDTO {

    private String id;
    private String departmentName;
    private String departmentType;
    private String description;
    private String parentId;
    private String resourceId;
    private String resourceName;
    private ResourceTypeEnum resourceType;
    private String resourceParentId;

    public DepartmentWithResourceDTO(String id, String departmentName, String departmentType
            , String description, String parentId, String resourceId
            , String resourceName, ResourceTypeEnum resourceType, String resourceParentId) {
        this.id = id;
        this.departmentName = departmentName;
        this.departmentType = departmentType;
        this.description = description;
        this.parentId = parentId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceParentId = resourceParentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(String departmentType) {
        this.departmentType = departmentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public String getResourceParentId() {
        return resourceParentId;
    }

    public void setResourceParentId(String resourceParentId) {
        this.resourceParentId = resourceParentId;
    }
}
