package tech.tongyu.bct.acl.common;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tech.tongyu.bct.auth.dto.UserDTO;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserStatus {

    private String username;
    private Boolean locked;
    private Boolean expired;
    private Collection<String> roles;
    private String token;
    private String refreshToken;
    private Boolean loginStatus;
    private String message;
    private String code;
    private String userId;

    public UserStatus(){ }

    public UserStatus(UserDTO user, String token, String message, String code, String userId, String refreshToken) {
        this.username = user.getUsername();
        this.locked = user.getLocked();
        this.expired = user.getExpired();
        this.roles = user.getRoleName();
        this.token = token;
        this.loginStatus = !Objects.isNull(token);
        this.message = message;
        this.code = code;
        this.userId = userId;
        this.refreshToken = refreshToken;
    }

    public UserStatus(UserDTO user, String token, String message, String code, String userId) {
        this.username = user.getUsername();
        this.locked = user.getLocked();
        this.expired = user.getExpired();
        this.roles = user.getRoleName();
        this.token = token;
        this.loginStatus = !Objects.isNull(token);
        this.message = message;
        this.code = code;
        this.userId = userId;
    }

    public UserStatus(UserDTO user, String token, String message, String code){
        this.username = user.getUsername();
        this.locked = user.getLocked();
        this.expired = user.getExpired();
        this.roles = user.getRoleName();
        this.token = token;
        this.loginStatus = !Objects.isNull(token);
        this.message = message;
        this.code = code;
    }

    public UserStatus(UserDTO user, String message, String code){
        this.username = user.getUsername();
        this.locked = user.getLocked();
        this.expired = user.getExpired();
        this.roles = user.getRoleName();
        this.loginStatus = false;
        this.message = message;
        this.code = code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(Boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
