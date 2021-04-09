package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.auth.dao.DaoConfig;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_ROLE, indexes = {
        @Index(name = EntityConstants.INDEX_ROLE__ROLE_NAME
                , columnList = EntityConstants.ROLE_NAME)
})
public class RoleDbo extends BaseEntity implements Serializable {
    @Column(name = EntityConstants.ROLE_NAME)
    private String roleName;

    @Column(name = EntityConstants.REMARK, length = DaoConfig.LIMIT.LENGTH_ROLE_REMARK)
    private String remark;

    @Column(name = EntityConstants.ALIAS)
    private String alias;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = EntityConstants.AUTH_SERVICE
            , name = EntityConstants.AUTH_USER_ROLE
            , joinColumns = {@JoinColumn(name = EntityConstants.ROLE_ID, referencedColumnName = EntityConstants.ID)}
            , inverseJoinColumns = {@JoinColumn(name = EntityConstants.USER_ID, referencedColumnName = EntityConstants.ID)})
    private Collection<UserDbo> userDbos;

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

    public Collection<UserDbo> getUserDbos() {
        return userDbos;
    }

    public void setUserDbos(Collection<UserDbo> userDbos) {
        this.userDbos = userDbos;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
