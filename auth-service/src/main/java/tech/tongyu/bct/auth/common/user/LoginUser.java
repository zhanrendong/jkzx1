package tech.tongyu.bct.auth.common.user;

import org.apache.commons.lang3.StringUtils;

import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.SM4Utils;

import java.util.Map;
import java.util.Objects;

public class LoginUser {

    private String username;
    private String password;
    private String captcha;

    public static LoginUser ofEncrypted(Map<String, Object> body, SM4Utils sm4Utils){
        String username = (String) body.get(AuthConstants.USERNAME);
        if(StringUtils.isBlank(username))
            username = (String) body.get(AuthConstants.USER_NAME);
        String password = (String) body.get(AuthConstants.PASSWORD);
        String captcha = (String) body.get(AuthConstants.CAPTCHA);
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_NOT_VALID);

        if (Objects.isNull(sm4Utils)){
            sm4Utils = new SM4Utils();
        }
        return new LoginUser(sm4Utils.decryptData_CBC(username), sm4Utils.decryptData_CBC(password), captcha);
    }

    public static LoginUser of(Map<String, Object> body, SM4Utils sm4Utils) {
        String username = (String) body.get(AuthConstants.USERNAME);
        if(StringUtils.isBlank(username))
            username = (String) body.get(AuthConstants.USER_NAME);
        String password = (String) body.get(AuthConstants.PASSWORD);
        String captcha = (String) body.get(AuthConstants.CAPTCHA);
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT);
        try {
            if (Objects.isNull(sm4Utils)){
                sm4Utils = new SM4Utils();
            }
            password = sm4Utils.decryptData_ECB(password);
            username = sm4Utils.decryptData_ECB(username);
        }catch (Exception e){
            throw new CustomException(ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT.getMessage());
        }
        return new LoginUser(username, password, captcha);
    }

    public LoginUser(String username, String password, String captcha){
        this.username = username;
        this.password = password;
        this.captcha = captcha;
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

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

}
