package tech.tongyu.bct.workflow.dto;

import java.util.Collection;

public class UserDTO {

    public UserDTO(String userName, Collection<String> roles) {
        this.userName = userName;
        this.roles = roles;
    }

    private String userName;
    private Collection<String> roles;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }
}
