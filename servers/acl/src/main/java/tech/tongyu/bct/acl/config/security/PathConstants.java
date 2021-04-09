package tech.tongyu.bct.acl.config.security;

public interface PathConstants {

    /*------------------------ static resources  --------------------------*/
    String FAVICON = "/favicon.ico";
    String SUFFIX_HTML = "/**/*.html";
    String SUFFIX_PNG = "/**/*.png";
    String SUFFIX_CSS = "/**/*.css";
    String SUFFIX_JS = "/**/*.js";
    /*------------------------ static resources  --------------------------*/

    /*------------------------ websocket related  --------------------------*/
    String WS_ANY = "/ws/**";
    String WS_REPORT = "/ws-report";
    String WS_REPORT_ANY = "/ws-report/**";
    String WS_END_POINT_ANY = "/ws-end-point/**";
    String WS_END_POINT = "/ws-end-point";
    String WS_END_POINT_INFO = "/ws-end-point/info";
    /*------------------------ websocket related  --------------------------*/

    /*------------------------ API  --------------------------*/
    String API_ANY = "/api/**";
    String FILE_DOWNLOAD = "/bct/download";
    /*------------------------ API  --------------------------*/

    /*------------------------ auth related  --------------------------*/
    String USERS_LOGIN = "/users/login";
    String USERS_TOKEN_LOGIN = "/users/token/login";
    String USERS_ENCRYPTED_LOGIN = "/users/encrypted/login";
    String USERS_CAPTCHA = "/users/captcha";
    String USERS_CHANGE_PASSWORD = "/users/password-change";
    String USERS_ENCRYPTED_CHANGE_PASSWORD = "/users/encrypted/password-change";
    String USERS_VERIFY_IP_TOKEN_BOUND = "/users/verify-ip-token";
    String API_DOC = "/api/method/list";
    /*------------------------ auth related  --------------------------*/

    String[] ARRAY_GET_PATHS = new String[]{
            FAVICON, SUFFIX_HTML, SUFFIX_JS, SUFFIX_CSS, SUFFIX_PNG
            , WS_ANY, WS_END_POINT, WS_END_POINT_ANY,WS_REPORT
            , USERS_CAPTCHA, FILE_DOWNLOAD, WS_REPORT_ANY};

    String[] ARRAY_POST_PATHS = new String[]{
            API_ANY
//            , USERS_LOGIN
            , WS_END_POINT_ANY
            , WS_REPORT, WS_REPORT_ANY};
}
