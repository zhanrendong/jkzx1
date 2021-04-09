package tech.tongyu.bct.acl.utils;

public interface TokenConstants {

    String TOKEN_KEY = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

    /*------------------------ token key  --------------------------*/
    String USERNAME = "username";
    String TOKEN = "token";
    String SUB = "sub";
    String EXP = "exp";
    String ISS = "iss";
    String IP = "ip";
    /*------------------------ token key  --------------------------*/

    String INVALID_TOKEN = "INVALID_TOKEN";
    String NEW_LOGIN_USER = "NEW_LOGIN_USER";

    String TOKEN_INVALID_MSG = "无效的登陆信息";
}
