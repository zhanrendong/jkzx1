package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.auth.enums.UserTypeEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_USER, indexes = {
        @Index(name = EntityConstants.INDEX_USER__USERNAME, columnList = EntityConstants.USERNAME)
        , @Index(name = EntityConstants.INDEX_USER__USER_TYPE, columnList = EntityConstants.USER_TYPE)
        , @Index(name = EntityConstants.INDEX_USER__LOCKED, columnList = EntityConstants.LOCKED)
        , @Index(name = EntityConstants.INDEX_USER__EXPIRED, columnList = EntityConstants.EXPIRED)})
public class UserDbo extends BaseEntity implements Serializable {

    @Column(name = EntityConstants.USERNAME)
    private String username;

    @Column(name = EntityConstants.NICK_NAME)
    private String nickName;

    @Column(name = EntityConstants.CONTACT_EMAIL)
    private String contactEmail;

    @Column(name = EntityConstants.PASSWORD)
    private String password;

    @Column(name = EntityConstants.USER_TYPE)
    @Enumerated(EnumType.STRING)
    private UserTypeEnum userType;

    @Column(name = EntityConstants.LOCKED)
    private Boolean locked;

    @Column(name = EntityConstants.EXPIRED)
    private Boolean expired;

    @Column(name = EntityConstants.TIMES_OF_LOGIN_FAILURE)
    private Integer timesOfLoginFailure;

    @Column(name = EntityConstants.PASSWORD_EXPIRED_TIMESTAMP)
    private Timestamp passwordExpiredTimestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = EntityConstants.DEPARTMENT_ID
            , referencedColumnName = EntityConstants.ID)
    private DepartmentDbo departmentDbo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = EntityConstants.AUTH_SERVICE
            , name = EntityConstants.AUTH_USER_ROLE
            , indexes = @Index(name = EntityConstants.INDEX_USER_ROLE__USER_ID_ROLE_ID, columnList = EntityConstants.USER_ID + ", " + EntityConstants.ROLE_ID)
            , joinColumns = {@JoinColumn(name = EntityConstants.USER_ID, referencedColumnName = EntityConstants.ID)}
            , inverseJoinColumns = {@JoinColumn(name = EntityConstants.ROLE_ID, referencedColumnName = EntityConstants.ID)})
    private Collection<RoleDbo> roleDbos;

    public UserDbo() {
        super();
    }

    public UserDbo(String username, String nickName
            , String contactEmail, String password
            , UserTypeEnum userType, Boolean locked, Boolean expired
            , Integer timesOfLoginFailure, Timestamp passwordExpiredTimestamp, DepartmentDbo departmentDbo
            , Collection<RoleDbo> roleDbos) {
        super();
        this.contactEmail = contactEmail;
        this.username = username;
        this.nickName = nickName;
        this.password = password;
        this.userType = userType;
        this.locked = locked;
        this.expired = expired;
        this.timesOfLoginFailure = timesOfLoginFailure;
        this.passwordExpiredTimestamp = passwordExpiredTimestamp;
        this.departmentDbo = departmentDbo;
        this.roleDbos = roleDbos;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<RoleDbo> getRoleDbos() {
        return roleDbos;
    }

    public void setRoleDbos(Collection<RoleDbo> roleDbos) {
        this.roleDbos = roleDbos;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Integer getTimesOfLoginFailure() {
        return timesOfLoginFailure;
    }

    public void setTimesOfLoginFailure(Integer timesOfLoginFailure) {
        this.timesOfLoginFailure = timesOfLoginFailure;
    }

    public Timestamp getPasswordExpiredTimestamp() {
        return passwordExpiredTimestamp;
    }

    public void setPasswordExpiredTimestamp(Timestamp passwordExpiredTimestamp) {
        this.passwordExpiredTimestamp = passwordExpiredTimestamp;
    }

    public UserTypeEnum getUserType() {
        return userType;
    }

    public void setUserType(UserTypeEnum userType) {
        this.userType = userType;
    }

    public DepartmentDbo getDepartmentDbo() {
        return departmentDbo;
    }

    public void setDepartmentDbo(DepartmentDbo departmentDbo) {
        this.departmentDbo = departmentDbo;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
