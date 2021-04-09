package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.RESOURCE_PERMISSION, indexes = {
        @Index(name = EntityConstants.INDEX_RESOURCE_PERMISSION__USER_ID, columnList = EntityConstants.USER_ID)
        , @Index(name = EntityConstants.INDEX_RESOURCE_PERMISSION__RESOURCE_ID, columnList = EntityConstants.RESOURCE_ID)})
public class ResourcePermissionDbo extends BaseEntity implements Serializable {

    @Column(name = EntityConstants.USER_ID)
    private String userId;

    @Column(name = EntityConstants.RESOURCE_ID)
    private String resourceId;

    @Column(name = EntityConstants.RESOURCE_PERMISSION_TYPE)
    @Enumerated(EnumType.STRING)
    private ResourcePermissionTypeEnum resourcePermissionType;

    public ResourcePermissionDbo() {
        super();
    }

    public ResourcePermissionDbo(String userId, String resourceId, String resourcePermissionType) {
        super();
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourcePermissionType = ResourcePermissionTypeEnum.of(resourcePermissionType);
    }

    public ResourcePermissionDbo(String userId, String resourceId, ResourcePermissionTypeEnum resourcePermissionTypeEnum){
        super();
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourcePermissionType = resourcePermissionTypeEnum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
