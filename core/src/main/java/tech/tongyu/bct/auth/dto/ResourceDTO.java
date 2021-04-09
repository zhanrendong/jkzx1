package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.util.tree.PlainTreeRecord;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

public class ResourceDTO implements PlainTreeRecord {

    @Override
    public Integer getSort() {
        return 0;
    }

    @BctField(
            name = "id",
            description = "资源ID",
            type = "String",
            order = 1
    )
    private String id;
    @BctField(
            name = "resourceName",
            description = "资源名称",
            type = "String",
            order = 2
    )
    private String resourceName;
    @BctField(
            name = "resourceType",
            description = "资源类型",
            type = "ResourceTypeEnum",
            order = 3,
            componentClass = ResourceTypeEnum.class
    )
    private ResourceTypeEnum resourceType;
    @BctField(
            name = "parentId",
            description = "父资源ID",
            type = "String",
            order = 4
    )
    private String parentId;
    @BctField(
            name = "departmentId",
            description = "部门ID",
            type = "String",
            order = 5
    )
    private String departmentId;
    @BctField(
            name = "createTime",
            description = "创建时间",
            type = "String",
            order = 6
    )
    private String createTime;

    public ResourceDTO(String id, String resourceName, ResourceTypeEnum resourceType, String parentId, String departmentId, String createTime) {
        this.id = id;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.parentId = parentId;
        this.departmentId = departmentId;
        this.createTime = createTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
}
