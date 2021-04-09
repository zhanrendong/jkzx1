package tech.tongyu.bct.acl.config.security.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(){
        super("权限错误，请检查用户名与密码并像管理员询问是否对所需的资源有访问权限");
    }
}
