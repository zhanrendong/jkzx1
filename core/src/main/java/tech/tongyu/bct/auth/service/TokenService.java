package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.UserDTO;

import java.util.Optional;

public interface TokenService {

//    /**
////     * 根据用户名和密码返回特定用户的token
////     * @param userName 用户名
////     * @param password 密码
////     * @return token
////     */
////    String getToken(String userName, String password);
////
////    /**
////     * 根据用户名和密码获取该用户的登录信息，用户名，所拥有的角色集合，所能看到的页面集合，token等等
////     * @param userName 用户名
////     * @param password 密码
////     * @return 返回上述登录信息的一个封装
////     */
////    UserDTO getLoginInfo(String userName, String password);

//    Optional<String> verifyAndGetToken(UserDTO userDto, String password);
    Optional<String> verifyAndGetResponseJson(UserDTO userDto, String password, String ip);
    Optional<String> verifyAndGetEncryptedResponseJson(UserDTO userDTO, String password, String ip);

    void upsertIpTokenBind(String ip, String token);
    Boolean verifyIpAndTokenBound(String ip, String token);
    Boolean verifyIpAndUsernameBound(String ip, String username);
}
