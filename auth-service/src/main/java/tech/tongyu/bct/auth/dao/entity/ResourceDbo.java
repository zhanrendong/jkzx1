package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.common.util.tree.PlainTreeRecord;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_RESOURCE, indexes = {
        @Index(name = EntityConstants.INDEX_RESOURCE__RESOURCE_NAME_RESOURCE_TYPE
                , columnList = EntityConstants.RESOURCE_NAME + ", " + EntityConstants.RESOURCE_TYPE)})
public class ResourceDbo extends BaseEntity implements Serializable, PlainTreeRecord {
    @Column(name = EntityConstants.RESOURCE_NAME)
    private String resourceName;

    @Column(name = EntityConstants.RESOURCE_TYPE)
    @Enumerated(EnumType.STRING)
    private ResourceTypeEnum resourceType;

    @Column(name = EntityConstants.PARENT_ID)
    private String parentId;

    @Column(name = EntityConstants.DEPARTMENT_ID)
    private String departmentId;

    @Column(name = EntityConstants.SORT)
    private Integer sort;

    public ResourceDbo() {
        super();
    }

    @Override
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public ResourceDbo(String resourceName, ResourceTypeEnum resourceType, String parentId, Integer sort) {
        super();
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.parentId = parentId;
        this.sort = sort;
    }

    public ResourceDbo(String resourceName, ResourceTypeEnum resourceType, String parentId, String departmentId, Integer sort){
        super();
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.parentId = parentId;
        this.departmentId = departmentId;
        this.sort = sort;
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

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
}
