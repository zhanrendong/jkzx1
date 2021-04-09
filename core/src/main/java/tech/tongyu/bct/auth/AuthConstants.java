package tech.tongyu.bct.auth;

public interface AuthConstants {

    String USERNAME = "username";
    String USER_NAME = "userName";
    String PASSWORD = "password";
    String NEW_PASSWORD = "newPassword";
    String OLD_PASSWORD = "oldPassword";
    String CAPTCHA = "captcha";
    String WEB_LOGIN = "webLogin";
    Integer SERVICE_ID = 16010;

    String ADMIN = "admin";

    String EXPIRATION_DAYS = "expiration_days";
    String MAX_LOGIN_FAILURE_TIMES = "max_login_failure_times";
    String DEFAULT_NAMESPACE__USER = "user";
    String TOKEN_VALID_IP_BOND = "token_valid_ip_bond";

    Integer PASSWORD_LENGTH_MIN = 12;
    Integer PASSWORD_LENGTH_MAX = 30;
}
