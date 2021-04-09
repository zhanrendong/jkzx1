package tech.tongyu.bct.common.api.request;

public class AuthRequest {
    private String userName;

    private String password;

    private String captcha;

    public AuthRequest(String userName, String password, String captcha) {
        this.userName = userName;
        this.password = password;
        this.captcha = captcha;
    }

    public AuthRequest() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
