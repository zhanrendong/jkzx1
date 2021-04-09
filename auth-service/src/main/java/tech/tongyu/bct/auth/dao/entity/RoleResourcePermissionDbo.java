package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

import javax.persistence.*;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.ROLE_RESOURCE_PERMISSION, indexes = {
        @Index(name = EntityConstants.INDEX_ROLE_RESOURCE_PERMISSION__ROLE_ID, columnList = EntityConstants.ROLE_ID)
        , @Index(name = EntityConstants.INDEX_ROLE_RESOURCE_PERMISSION__RESOURCE_ID, columnList = EntityConstants.RESOURCE_ID)})
public class RoleResourcePermissionDbo extends BaseEntity {

    @Column(name = EntityConstants.ROLE_ID)
    private String roleId;

    @Column(name = EntityConstants.RESOURCE_ID)
    private String resourceId;

    @Column(name = EntityConstants.RESOURCE_PERMISSION_TYPE)
    @Enumerated(EnumType.STRING)
    private ResourcePermissionTypeEnum resourcePermissionType;

    public RoleResourcePermissionDbo() {
        super();
    }

    public RoleResourcePermissionDbo(String roleId, String resourceId, String resourcePermissionType) {
        super();
        this.roleId = roleId;
        this.resourceId = resourceId;
        this.resourcePermissionType = ResourcePermissionTypeEnum.of(resourcePermissionType);
    }

    public RoleResourcePermissionDbo(String roleId, String resourceId, ResourcePermissionTypeEnum resourcePermissionTypeEnum){
        super();
        this.roleId = roleId;
        this.resourceId = resourceId;
        this.resourcePermissionType = resourcePermissionTypeEnum;
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
