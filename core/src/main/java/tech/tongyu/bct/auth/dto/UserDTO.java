package tech.tongyu.bct.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.util.CustomDateSerializer;
import tech.tongyu.bct.auth.enums.UserTypeEnum;

import java.sql.Timestamp;
import java.util.Collection;

public class UserDTO {

    @BctField(
            name = "id",
            description = "用户唯一ID",
            type = "String",
            order = 1
    )
    private String id;
    @BctField(
            name = "username",
            description = "用户名",
            type = "String",
            order = 2
    )
    private String username;
    @BctField(
            name = "nickName",
            description = "昵称",
            type = "String",
            order = 3
    )
    private String nickName;
    @BctField(
            name = "contactEmail",
            description = "联系邮箱",
            type = "String",
            order = 4
    )
    private String contactEmail;
    @BctField(
            name = "userType",
            description = "用户类型",
            type = "UserTypeEnum",
            order = 5,
            componentClass = UserTypeEnum.class
    )
    private UserTypeEnum userType;
    @BctField(
            name = "locked",
            description = "是否锁定",
            type = "Boolean",
            order = 6
    )
    private Boolean locked;
    @BctField(
            name = "expired",
            description = "是否过期",
            type = "Boolean",
            order = 7
    )
    private Boolean expired;
    @BctField(
            name = "timesOfLoginFailure",
            description = "登陆失败次数",
            type = "Integer",
            order = 8
    )
    private Integer timesOfLoginFailure;
    @BctField(
            name = "departmentId",
            description = "部门ID",
            type = "String",
            order = 9
    )
    private String departmentId;

    @BctField(
            name = "password",
            description = "密码",
            type = "String",
            order = 10
    )
    @JsonIgnore
    private String password;

    @BctField(
            name = "passwordExpiredTimestamp",
            description = "密码失效时间",
            type = "Timestamp",
            order = 11
    )
    @JsonSerialize(using = CustomDateSerializer.class)
    private Timestamp passwordExpiredTimestamp;

    @BctField(
            name = "roleName",
            description = "角色名",
            type = "Collection<String>",
            order = 12
    )
    private Collection<String> roleName;

    public UserDTO() {
    }

    public UserDTO(String id, String username, String nickName, String contactEmail, String password
            , UserTypeEnum userType, Boolean locked, Boolean expired, Integer timesOfLoginFailure
            , Timestamp passwordExpiredTimestamp, String departmentId, Collection<String> roleName) {
        this.id = id;
        this.username = username;
        this.nickName = nickName;
        this.contactEmail = contactEmail;
        this.userType = userType;
        this.password = password;
        this.locked = locked;
        this.expired = expired;
        this.timesOfLoginFailure = timesOfLoginFailure;
        this.passwordExpiredTimestamp = passwordExpiredTimestamp;
        this.departmentId = departmentId;
        this.roleName = roleName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public UserTypeEnum getUserType() {
        return userType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserType(UserTypeEnum userType) {
        this.userType = userType;
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

    public Collection<String> getRoleName() {
        return roleName;
    }

    public void setRoleName(Collection<String> roleName) {
        this.roleName = roleName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean isLoginPermitted(){
        return !getExpired() && !getLocked();
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
