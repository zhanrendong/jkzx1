package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_PAGE_PERMISSION)
public class PagePermissionDbo extends BaseEntity implements Serializable {

    @Column(name = EntityConstants.ROLE_ID)
    private String roleId;

    @Column(name = EntityConstants.PAGE_COMPONENT_ID)
    private String pageComponentId;

    public PagePermissionDbo() {
        super();
    }

    public PagePermissionDbo(String roleId, String pageComponentId) {
        this.roleId = roleId;
        this.pageComponentId = pageComponentId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPageComponentId() {
        return pageComponentId;
    }

    public void setPageComponentId(String pageComponentId) {
        this.pageComponentId = pageComponentId;
    }
}
