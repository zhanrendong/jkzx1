package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public class UserStatusDTO {

    @BctField(
            name = "username",
            description = "用户名",
            order = 1
    )
    private String username;
    @BctField(
            name = "locked",
            description = "是否锁定",
            order = 2
    )
    private Boolean locked;
    @BctField(
            name = "expired",
            description = "是否过期",
            order = 3
    )
    private Boolean expired;

    public UserStatusDTO(String username, Boolean locked, Boolean expired) {
        this.username = username;
        this.locked = locked;
        this.expired = expired;
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
}
