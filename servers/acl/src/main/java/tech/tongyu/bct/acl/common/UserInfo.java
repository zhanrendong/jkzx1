package tech.tongyu.bct.acl.common;

public class UserInfo {

    private String userName;

    private String token;

    public UserInfo(String userName, String token) {
        this.userName = userName;
        this.token = token;
    }

    public UserInfo() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
